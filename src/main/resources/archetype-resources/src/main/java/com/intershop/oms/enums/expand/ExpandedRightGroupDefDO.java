package com.intershop.oms.enums.expand;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import bakery.persistence.annotation.ExpandedEnum;
import bakery.persistence.dataobject.Configuration;
import bakery.persistence.dataobject.configuration.common.RightGroupDefDO;
import bakery.persistence.expand.RightGroupDefDOEnumInterface;
import bakery.util.IdOwner;

@Entity
@Table(name = "`RightGroupDefDO`")
@Configuration
@ExpandedEnum( RightGroupDefDO.class )
public enum ExpandedRightGroupDefDO implements IdOwner<Integer> , RightGroupDefDOEnumInterface
{

	// start with 1000 to avoid conflicts with RightGroupDefDO
	// the name must be unique across both classes
	// values with negative id are meant as syntax example and are ignored (won't get persisted within the db)
	EXAMPLE( Integer.valueOf(-999), "exampleGroup"),
	CUSTOM_RIGHTS ( Integer.valueOf(1001), "Custom rights");

	private static final long serialVersionUID = 1;

	private Integer id;
	private String name;

	private ExpandedRightGroupDefDO( Integer id, String name)
	{
		this.id = id;
		this.name = name;
	}

	/** Returns the ID of the permission.
	 *
	 * @return the ID of the permission.
	 */
	@Override
	@Id
	public Integer getId() {
		return id;
	}

	/**
	 * Sets the ID of the permission.
	 *
	 * @return
	 */
	public void setId( Integer id ) {
		this.id = id;
	}

	/**
	 * Returns the name of the permission.
	 *
	 * @return the name of the permission.
	 */
	@Override
	@Column( name = "`name`", length = 255, nullable = false )
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the permission.
	 *
	 * @return
	 */
	public void setName( String name ) {
		this.name = name;
	}



}
