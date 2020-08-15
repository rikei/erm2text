package com.panpan.erm2text.plugin;

import com.panpan.erm2text.meta.Column;
import com.panpan.erm2text.meta.Database;
import com.panpan.erm2text.meta.Table;
import com.panpan.erm2text.util.ColumnUtil;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 
 * @ClassName MyBatis_MysqlMojo
 * @Description 
 * @author wosten
 * @Date 2017年3月7日 上午9:19:51
 * @version 1.0.0
 * @phase generate-sources
 * 
 * @goal mybatis_mysql
 * 
 */
public class MyBatis_MysqlMojo {
	
	/**
	 * @parameter
	 * @required
	 */
	private String basePackage;
	/**
	 * @parameter default-value="target/zeus-generated"
	 */
	private String outputDirectory;
	
	
	/**
	 * @parameter
	 */
	private String versionField;
	
	
	/**
	 * @parameter
	 * @required
	 */
	private File sources[];

	/**
	 * @parameter default-value=false
	 */
	private boolean trimStrings;
	
	/**
	 * @parameter default-value=false
	 */
	private boolean useAutoTrimType;
	
	/**
	 * @parameter default-value=".*"
	 */
	private String tablePattern;
	
    /**
     * <i>Maven Internal</i>: Project to interact with.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     * @noinspection UnusedDeclaration
     */
//    private MavenProject project;

	public void execute(String outputFile) throws Exception{
		try {

			List<Database> databases = new ArrayList<Database>();
			ERMImporter ermImporter = new ERMImporter(getLog());
			
			for (File source : sources)
			{
				getLog().info("处理源文件:" + source.getAbsolutePath());
				String ext = FilenameUtils.getExtension(source.getName());
				 if ("erm".equals(ext))
					databases.add(ermImporter.doImport(source, tablePattern));
				else
					throw new Exception("不支持的扩展名[" + ext + "]");
			}

			StringBuffer sb = new StringBuffer();
			for (Database db : databases)
			{
				List<Table> tableList = db.getTables();
				tableList.sort(new Comparator<Table>() {
					@Override
					public int compare(Table o1, Table o2) {
						return o1.getDbName().compareTo(o2.getDbName());
					}
				});
				for(Table table: tableList){
					sb.append(String.format("%s %s\n", table.getDbName(), table.getTextName()));
					sb.append("\t=========== Column ==========\n");
					for (Column column: table.getColumns()){
						//sb.append(String.format("\t%s %s %s %d %d\n",
						//		column.getDbName(),column.getTextName(),column.getDbType(),column.getLength(),column.getScale()));
						sb.append(String.format("\t%s %s %s %s %s %s\n",
							column.getDbName(),column.getTextName(),
								ColumnUtil.getColumnType(column.getDbType(),column.getLength(),column.getScale()), column.isMandatory() ? "nullable" : "notNull",
								column.getDescription().replaceAll("[\\t\\n\\r]"," "),
								column.isIdentity()? "auto_increment":""));
					}
					sb.append("\t=========== Index ==========\n");
					for (List<Column> columns: table.getIndexes()){
						List<String> index = new ArrayList<String>();
						for (Column c: columns){
							index.add(c.getDbName());
						}
						sb.append(String.format("\t%s\n", String.join("-", index)));
					}

					sb.append("\t=========== Unique ==========\n");
					for (List<Column> uniques: table.getUniques()){
						List<String> unique = new ArrayList<String>();
						for (Column c: uniques){
							unique.add(c.getDbName());
						}
						sb.append(String.format("\t%s\n", String.join("-", unique)));
					}
				}
			}

			FileWriter fileWriter = new FileWriter(new File(outputFile));
			fileWriter.write(sb.toString());
			fileWriter.close();
		} catch (Exception e) {
			throw new Exception("生成过程出错", e);
		}

	}

	private Logger getLog() {
		return LoggerFactory.getLogger(getClass());
	}

	public String getOutputDirectory()
	{
		return outputDirectory;
	}

	public void setOutputDirectory(String outputDirectory)
	{
		this.outputDirectory = outputDirectory;
	}

	public boolean isTrimStrings()
	{
		return trimStrings;
	}

	public void setTrimStrings(boolean trimStrings)
	{
		this.trimStrings = trimStrings;
	}

	public String getBasePackage()
	{
		return basePackage;
	}

	public void setBasePackage(String basePackage)
	{
		this.basePackage = basePackage;
	}

	public boolean isUseAutoTrimType() {
		return useAutoTrimType;
	}

	public void setUseAutoTrimType(boolean useAutoTrimType) {
		this.useAutoTrimType = useAutoTrimType;
	}

	public File[] getSources() {
		return sources;
	}

	public void setSources(File[] sources) {
		this.sources = sources;
	}
	
	public String getVersionField() {
		return versionField;
	}

	public void setVersionField(String versionField) {
		this.versionField = versionField;
	}

}
