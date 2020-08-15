package com.panpan.erm2text.plugin;

import com.panpan.erm2text.meta.*;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.ibator.api.dom.java.FullyQualifiedJavaType;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 *导入ERMaster的定义文件
 * @author liuhs
 *
 */
public class ERMImporter {
	
	private Logger logger;
	
	public ERMImporter(Logger logger)
	{
		this.logger = logger;
	}
	
	private FullyQualifiedJavaType fqjtInteger = new FullyQualifiedJavaType("java.lang.Integer");
	
	private FullyQualifiedJavaType fqjtBigDecimal = new FullyQualifiedJavaType("java.math.BigDecimal");
	
	private FullyQualifiedJavaType fqjtDate = new FullyQualifiedJavaType("java.util.Date");
	
	private static final Pattern hintPattern = Pattern.compile("\\[\\[.*\\]\\]");

	@SuppressWarnings("unchecked")
	public Database doImport(File ermSource, String tablePattern) throws DocumentException
	{
		Database result = new Database();
		// 解析源文件
		SAXReader sar = new SAXReader();
		Document docSource = sar.read(ermSource);
		//先取所有的word，组建Column
		Map<String, Element> words = new HashMap<String, Element>();
		Map<String, Domain> domains = new HashMap<String, Domain>();
		for (Element nodeWord : (List<Element>) docSource.selectNodes("/diagram/dictionary/word"))
		{
			String id = nodeWord.elementText("id");
			words.put(id, nodeWord);
			
			//Domain
			Domain domain = parseDomain(nodeWord.elementText("physical_name"), nodeWord.elementText("description"));
			if (domain != null)
			{
				domains.put(id, domain);
				result.getDomains().add(domain);
			}
		}
		//取出table
		Map<String, Element> tables = new HashMap<String, Element>();
		for (Element nodeTable : (List<Element>) docSource.selectNodes("/diagram/contents/table"))
			tables.put(nodeTable.elementText("id"), nodeTable);
		
		//开始组建Database对象
		Map<String, Column> allColumns = new HashMap<String, Column>();	//全局column映射，以id为key
		for (Element nodeTable : tables.values())
		{
			Table table = new Table();
			table.setDbName(nodeTable.elementText("physical_name").trim());
			table.setTextName(nodeTable.elementText("logical_name"));
			logger.debug(table.getDbName());
			
			Set<String> columnNames = new HashSet<String>();	//防重复

			for (Element nodeColumn : (List<Element>) nodeTable.selectNodes("columns/*"))
			{
				Column column = new Column();
				logger.debug(nodeColumn.getName());
				
				String word_id = nodeColumn.elementText("word_id");
				if (word_id == null)
				{
					//没找到的话就找referenced_column
					Element node = nodeColumn;
					
					do
					{
						String refId = node.elementText("referenced_column");
						if (refId == null)
							throw new IllegalArgumentException();
						node = (Element)docSource.selectSingleNode("//table/columns/*[id='" + refId + "']");
						word_id = node.elementText("word_id");
					}while (StringUtils.isEmpty(word_id));
				}
					
				Element nodeWord = words.get(word_id);
				//以本node的物理名优先
				String physicalName = nodeColumn.elementText("physical_name");
				if (StringUtils.isBlank(physicalName))
					physicalName = nodeWord.elementText("physical_name");
				column.setDbName(physicalName.trim());
				column.setIdentity(Boolean.valueOf(nodeColumn.elementText("auto_increment")));
				
				//逻辑名也一样处理
				String logicalName = nodeColumn.elementText("logical_name");
				if (StringUtils.isBlank(logicalName))
					logicalName = nodeWord.elementText("logical_name");

				column.setTextName(logicalName);
				column.setDescription(nodeWord.elementText("description"));
				column.setId(nodeColumn.elementText("id"));
				column.setMandatory(Boolean.parseBoolean(nodeColumn.elementText("not_null")));
				//从description解析hint
				column.setHint(extractHint(column.getDescription()));
				//解析类型
				String type = nodeWord.elementText("type");
				String length = nodeWord.elementText("length");
				String decimal = nodeWord.elementText("decimal");
				if ("char".equals(type))
				{
					column.setJavaType(FullyQualifiedJavaType.getStringInstance());
					column.setLength(1);
					column.setDbType("CHAR");
				}
				else if ("character(n)".equals(type) || "varchar(n)".equals(type))
				{
					column.setJavaType(FullyQualifiedJavaType.getStringInstance());
					column.setLength(Integer.parseInt(length));
					column.setDbType("VARCHAR");
				}
				else if ("decimal".equals(type))
				{
					logger.warn(MessageFormat.format("decimal没有指定长度，按1处理。[{0}], {1}, {2}", type, column.getDbName(), table.getDbName()));
//					column.setJavaType(fqjtInteger);
					column.setJavaType(new FullyQualifiedJavaType("java.lang.Long"));
					column.setLength(1);
					column.setDbType("BIGINT");
				}
				else if ("decimal(p)".equals(type)||"numeric(p)".equals(type))
				{
					int l = Integer.parseInt(length);
					if (l < 8 )
						column.setJavaType(fqjtInteger);
					else
						column.setJavaType(fqjtBigDecimal);
					column.setLength(l);
					column.setDbType("DECIMAL");
				}
				else if ("decimal(p,s)".equals(type)||"numeric(p,s)".equals(type))
				{
					int l = Integer.parseInt(length);
					int s = Integer.parseInt(decimal);
					if (s == 0 && l < 8 )
						column.setJavaType(fqjtInteger);
					else
						column.setJavaType(fqjtBigDecimal);
					column.setLength(l);
					column.setScale(s);
					column.setDbType("DECIMAL");
				}
				else if ("integer".equals(type))
				{
					column.setJavaType(fqjtInteger);
					column.setLength(9);
					column.setDbType("INTEGER");
				}
				else if ("bigint".equals(type))
				{

					column.setJavaType(new FullyQualifiedJavaType("java.lang.Long"));
					column.setLength(18);
					column.setDbType("INTEGER");
				}
				else if ("date".equals(type))
				{
					column.setJavaType(fqjtDate);
					column.setTemporal("DATE");
					column.setDbType("TIMESTAMP");
				}
				else if ("time".equals(type))
				{
					column.setJavaType(fqjtDate);
					column.setTemporal("TIME");
					column.setDbType("TIMESTAMP");
				}
				else if ("timestamp".equals(type)||"datetime".equals(type))
				{
					column.setJavaType(fqjtDate);
					column.setTemporal("TIMESTAMP");
					column.setDbType("TIMESTAMP");
				}
				else if ("clob".equals(type) || type.endsWith("text"))
				{
					column.setJavaType(FullyQualifiedJavaType.getStringInstance());
					column.setLob(true);
					column.setDbType("CLOB");
				}
				else if (type.endsWith("blob"))
				{
					column.setJavaType(new FullyQualifiedJavaType("byte[]"));
					column.setLob(true);
					column.setDbType("BLOB");
				}
				else
				{
					logger.warn(MessageFormat.format("无法识别的类型[{0}]，跳过, {1}, {2}", type, column.getDbName(), table.getDbName()));
					continue;
				}
				
				
				if (type.startsWith("numeric"))
					logger.warn(MessageFormat.format("建议不要使用numeric，用decimal代替[{0}], {1}, {2}", type, column.getDbName(), table.getDbName()));
				if (type.startsWith("datetime"))
					logger.warn(MessageFormat.format("建议不要使用datetime，用timestamp代替[{0}], {1}, {2}", type, column.getDbName(), table.getDbName()));
				
				
				column.setVersion(
					"JPA_VERSION".equalsIgnoreCase(column.getDbName()) ||
					"JPA_TIMESTAMP".equalsIgnoreCase(column.getDbName())
					);
				
				
				if ("true".equals(nodeColumn.elementText("unique_key")))
				{
					List<Column> unique = new ArrayList<Column>();
					unique.add(column);
					table.getUniques().add(unique);
				}
				
				
				if (columnNames.contains(column.getDbName()))	
				{
					logger.warn(MessageFormat.format("字段重复，跳过 {0}, {1}", column.getDbName(), table.getDbName()));
					continue;
				}
				columnNames.add(column.getDbName());
				
				allColumns.put(column.getId(), column);
				table.getColumns().add(column);
				
				
				if (Boolean.parseBoolean(nodeColumn.elementText("primary_key")))
				{
					table.getPrimaryKeyColumns().add(column);
				}
				
				//domain
				if (domains.containsKey(word_id))
				{
					//如果有domain，设置之
					column.setDomain(domains.get(word_id));

				}
			}
			if (table.getPrimaryKeyColumns().isEmpty())	
			{
				logger.warn(table.getDbName() + "没有主键，跳过");
				continue;
			}
			
			//处理索引
			for (Element nodeIndex : (List<Element>) nodeTable.selectNodes("indexes/*"))	//Դ�ļ���ƴд��������������*��ϣ���Ժ�汾��ĵ�(1.0.0)
			{
				List<Column> index = new ArrayList<Column>();
				for (Element nodeColumn : (List<Element>) nodeIndex.selectNodes("columns/column"))
				{
					index.add(allColumns.get(nodeColumn.elementText("id")));
				}
				table.getIndexes().add(index);
			}
			//唯一约束也按索引处理
			for (Element nodeIndex : (List<Element>) nodeTable.selectNodes("complex_unique_key_list/complex_unique_key"))
			{
				List<Column> unique = new ArrayList<Column>();
				for (Element nodeColumn : (List<Element>) nodeIndex.selectNodes("columns/column"))
				{
					unique.add(allColumns.get(nodeColumn.elementText("id")));
				}
				table.getUniques().add(unique);
			}
			
			result.getTables().add(table);
		}
		
		//处理Sequence
		for (Element nodeName : (List<Element>) docSource.selectNodes("/diagram/sequence_set/sequence/name"))
			result.getSequences().add(nodeName.getText());
		
		return result;
	}
	
	private Domain parseDomain(String code, String desc)
	{
		try
		{
			Domain domain = null;
			
			BufferedReader br = new BufferedReader(new StringReader(desc));
			String line = br.readLine();
			boolean started = false;
			while (line != null)
			{
			
				if (StringUtils.isNotBlank(line))
				{
					if (started)
					{
						if (line.startsWith("@"))
						{
							
							String type = StringUtils.remove(line.trim(), "@");
							domain.setType(new FullyQualifiedJavaType(type));
							domain.setCode(domain.getType().getShortName());
							Class<?> javaType = ClassUtils.getClass(type);
							EnumInfo enumInfo = javaType.getAnnotation(EnumInfo.class);
							if (enumInfo == null)
							{
								logger.warn(MessageFormat.format("类型{0}没有指定EnumInfo", type));
							}
							else
							{
								LinkedHashMap<String, String> valueMap = new LinkedHashMap<String, String>();
								for (String value : enumInfo.value())
								{
									String kv[] = value.split("\\|");
									if (kv.length != 2)
										throw new IllegalArgumentException("键值对语法错[" + javaType.getCanonicalName() + "]:" + value);
									String key = kv[0];
									key = StringUtils.replace(key, ".", "_");
									valueMap.put(key, kv[1]);
								}
								domain.setValueMap(valueMap);
							}
							break;
						}
						
						String kv[] = line.split("\\|");
						if (kv.length != 2)
							throw new IllegalArgumentException("键值对语法错[" + code + "]:" + line);
						String key = kv[0];
						key = StringUtils.replace(key, ".", "_");
						domain.getValueMap().put(key, kv[1]);
					}
					else if ("///".equals(StringUtils.trim(line)))
					{
						started = false;//true;
						domain = new Domain();
						domain.setCode(code);
						domain.setValueMap(new LinkedHashMap<String, String>());
					}
				}
				line = br.readLine();
			}
			return domain;
		}
		catch (Throwable t)
		{
			throw new IllegalArgumentException(t);
		}
	}
	
	public String extractHint(String desc)
	{
		Matcher m = hintPattern.matcher(desc);
		if (!m.find())
			return null;
		
		return desc.substring(m.start() + 2, m.end() - 2);
		
	}
}
