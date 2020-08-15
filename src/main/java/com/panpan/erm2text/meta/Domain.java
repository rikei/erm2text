package com.panpan.erm2text.meta;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.ibatis.ibator.api.dom.java.FullyQualifiedJavaType;

import java.util.LinkedHashMap;

public class Domain {
	private String code;
	private String name;
	private String dbType;
	/**
	 * 自身的全名，需由总控填写
	 */
	private FullyQualifiedJavaType type;
	
	private FullyQualifiedJavaType supportClientType;
	/**
	 * 可用值列表，key为取值，value为描述, 需要保持有序，所以使用 {@link LinkedHashMap}，这里不作类型处理
	 */
	private LinkedHashMap<String, String> valueMap;
	
	public boolean hasValueMap()
	{
		return valueMap != null && !valueMap.isEmpty();
	}
	public String getCode()
	{
		return code;
	}
	public void setCode(String code)
	{
		this.code = code;
	}
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	public String getDbType()
	{
		return dbType;
	}
	public void setDbType(String dbType)
	{
		this.dbType = dbType;
	}
	public LinkedHashMap<String, String> getValueMap()
	{
		return valueMap;
	}
	public void setValueMap(LinkedHashMap<String, String> valueMap)
	{
		this.valueMap = valueMap;
	}
	public FullyQualifiedJavaType getType()
	{
		return type;
	}
	public void setType(FullyQualifiedJavaType type)
	{
		this.type = type;
	}
	public FullyQualifiedJavaType getSupportClientType() {
		return supportClientType;
	}
	public void setSupportClientType(FullyQualifiedJavaType supportClientType) {
		this.supportClientType = supportClientType;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}
		Domain rhs = (Domain) obj;
		return new EqualsBuilder()
				.append(supportClientType, rhs.supportClientType)
				.append(type, rhs.type)
				.isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(type)
			.append(supportClientType)
			.toHashCode();
	}
}
