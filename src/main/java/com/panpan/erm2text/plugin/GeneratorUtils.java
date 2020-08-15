package com.panpan.erm2text.plugin;

import com.panpan.erm2text.meta.Column;
import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.ibatis.ibator.api.dom.java.*;
import org.apache.ibatis.ibator.internal.util.JavaBeansUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;


public class GeneratorUtils
{
	public static String dbName2ClassName(String dbName)
	{
		String s = dbName;
		
		boolean allUpperCaseOrNumeric = true;
		for (char c : s.toCharArray())
		{
			if (c != '_' && !CharUtils.isAsciiNumeric(c) && !CharUtils.isAsciiAlphaUpper(c))
			{
				allUpperCaseOrNumeric = false;
				break;
			}
		}
		
		if (allUpperCaseOrNumeric)
		{
			//为应对Java类定义的情况，只有在全大写时才需要定义
			//TODO 这是临时方案
			s = s.toLowerCase();
			s = WordUtils.capitalizeFully(s, new char[]{ '_' });
			s = StringUtils.remove(s, "_");
		}
		
		if (!StringUtils.isAlpha(StringUtils.left(s, 1)))
			s = "_" + s;
		return s;
	}

	public static String dbName2PropertyName(String dbName)
	{
		return WordUtils.uncapitalize(dbName2ClassName(dbName));
	}

	public static FullyQualifiedJavaType forType(TopLevelClass topLevelClass, String type)
	{
		FullyQualifiedJavaType fqjt = new FullyQualifiedJavaType(type);
		topLevelClass.addImportedType(fqjt);
		return fqjt;
	}

	public static Field generateProperty(TopLevelClass clazz, FullyQualifiedJavaType fqjt, String property, List<String> javadoc, boolean trimStrings,boolean keyColumn,int order)
	{
		clazz.addImportedType(fqjt);

		Field field = new Field();
		field.setVisibility(JavaVisibility.PRIVATE);
		field.setType(fqjt);
		if (keyColumn) {
			field.addAnnotation("@ZeusKey");
		}
		
		if (javadoc != null && javadoc.size()>0) {
			field.addAnnotation("@ZeusDoc(value=\""+javadoc.get(0)+"\",order="+order+")");
		}
		
		field.setName(property);
		clazz.addField(field);

		//getter
		Method method = new Method();
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setReturnType(fqjt);
		method.setName(JavaBeansUtil.getGetterMethodName(field.getName(), field.getType()));
		StringBuilder sb = new StringBuilder();
		sb.append("return ");
		sb.append(property);
		sb.append(';');
		method.addBodyLine(sb.toString());

		createJavadoc(method, javadoc);

		clazz.addMethod(method);

		//setter
		method = new Method();
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setName(JavaBeansUtil.getSetterMethodName(property));
		method.addParameter(new Parameter(fqjt, property));
		createJavadoc(method, javadoc);

		if (trimStrings && fqjt.equals(FullyQualifiedJavaType.getStringInstance()))
		{
			sb.setLength(0);
			sb.append("this."); //$NON-NLS-1$
			sb.append(property);
			sb.append(" = "); //$NON-NLS-1$
			sb.append(property);
			sb.append(" == null ? null : "); //$NON-NLS-1$
			sb.append(property);
			sb.append(".trim();"); //$NON-NLS-1$
			method.addBodyLine(sb.toString());
		}
		else
		{
			sb.setLength(0);
			sb.append("this."); //$NON-NLS-1$
			sb.append(property);
			sb.append(" = "); //$NON-NLS-1$
			sb.append(property);
			sb.append(';');
			method.addBodyLine(sb.toString());
		}

		clazz.addMethod(method);

		return field;
	}

	private static void createJavadoc(Method method, List<String> javadoc) {
		if (javadoc != null)
		{
			method.addJavaDocLine("/**");
			for (String line : javadoc)
			{
				method.addJavaDocLine(" * <p>" + line + "</p>");
			}
			method.addJavaDocLine(" */");
		}
	}
	
	/**
	 * @param col
	 * @return
	 */
	public static List<String> generatePropertyJavadoc(Column col)
	{
		try
		{
			List<String> result = new ArrayList<String>();
			result.add(col.getTextName());
			
			String desc = col.getDescription();
			if (StringUtils.isNotBlank(desc))
			{
				BufferedReader br = new BufferedReader(new StringReader(desc));
				String line = br.readLine();
				while (line != null)
				{
					if (line.equals("///"))
						break;
					line = StringUtils.remove(line, "[[");
					line = StringUtils.remove(line, "]]");
					result.add(line);
					line = br.readLine();
				}
			}
		
			return result;
		} 
		catch (IOException e) {
			//�������
			throw new IllegalArgumentException(e);
		}
	}
}
