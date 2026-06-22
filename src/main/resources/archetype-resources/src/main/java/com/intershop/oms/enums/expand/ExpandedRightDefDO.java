package com.intershop.oms.enums.expand;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;

import bakery.persistence.annotation.ExpandedEnum;
import bakery.persistence.dataobject.configuration.common.RightDefDO;
import bakery.persistence.dataobject.configuration.common.RightGroupDefDO;
import bakery.persistence.expand.RightDefDOEnumInterface;
import bakery.persistence.expand.RightGroupDefDOEnumInterface;
import bakery.util.IdOwner;

@ExpandedEnum (RightDefDO.class )
public enum ExpandedRightDefDO implements RightDefDOEnumInterface
{


    // start with 10000 to avoid conflicts with RightDefDO
	// the name must be unique across both classes
	// values with negative id are meant as syntax example and are ignored (won't get persisted within the db)
	EXAMPLE_1( Integer.valueOf(-888), "exampleRight_1", "exampleRight 1 explained.",    RightGroupDefDO.CONFIGURATION, false, "example.right.key.1"),
	EXAMPLE_2( Integer.valueOf(-999), "exampleRight_2", "exampleRight 2 explained.",    ExpandedRightGroupDefDO.EXAMPLE, false, "example.right.key.2"),
    CUSTOM_RIGHT( Integer.valueOf(10001), "Custom right", "Example for an expanded custom right.", ExpandedRightGroupDefDO.CUSTOM_RIGHTS, false, "custom.right.key" );

	private static final long serialVersionUID = 1;

	private Integer id;
	private String name;
	private String description;
	private RightGroupDefDOEnumInterface rightGroupDefDO;
	private Integer rightGroupDefRef;
	private boolean includeNonDeliveringSupplier;
	private String right;

	private ExpandedRightDefDO( Integer id, String name, String description, RightGroupDefDOEnumInterface rightGroupDefDO, boolean includeNonDeliveringSupplier, String right )
	{
		this.right = right;
		this.id = id;
		this.name = name;
		this.description = description;
		setRightGroupDefRef( rightGroupDefDO.getId() );
		setIncludeNonDeliveringSupplier( includeNonDeliveringSupplier );
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

	/**
	 * Returns the description of the permission.
	 *
	 * @return the description of the permission.
	 */
	@Column( name = "`description`", length = 1000, nullable = false )
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description of the permission.
	 *
	 * @return
	 */
	public void setDescription( String description ) {
		this.description = description;
	}

	/**
	 * Returns the permission group of the permission.
	 *
	 * @return the permission group of the permission.
	 */
	@Override
	@Transient
	public RightGroupDefDOEnumInterface getRightGroupDefDO() {
	    if (null==rightGroupDefDO)
	    {
	        // TODO check why this is required
	        rightGroupDefDO =RightGroupDefDO.getRightGroupDefDO(this.rightGroupDefRef);
	    }

		return rightGroupDefDO;
	}

	/**
	 * Checks whether a permission is assigned to a given permission group or not.
	 *
	 * @return whether a permission is assigned to a given permission group or not.
	 */
	@Override
    @Transient
	public boolean isInRightGroup( RightGroupDefDOEnumInterface defDO ) {
		if ( defDO == null ) {
			return false;
		}

		return rightGroupDefDO.equals( defDO ) ? true : false;
	}

	/**
	 * Sets the permission group of the permission.
	 *
	 * @return
	 */
	@Override
    public void setRightGroupDefDO( RightGroupDefDOEnumInterface rightGroupDefDO ) {
		this.rightGroupDefDO = rightGroupDefDO;
	}

	/**
	 * Returns the permission group ID of the permission.
	 *
	 * @return the permission group ID of the permission.
	 */
	@Override
    @Column( name = "`rightGroupDefRef`", nullable = false )
	public Integer getRightGroupDefRef() {
		return rightGroupDefRef;
	}

	/**
	 * Sets the permission group of the permission by given ID.
	 *
	 * @return
	 */
	@Override
    public void setRightGroupDefRef( Integer rightGroupDefRef ) {
		this.rightGroupDefRef = rightGroupDefRef;
		rightGroupDefDO = RightGroupDefDO.getRightGroupDefDO( rightGroupDefRef );
	}

	/**
	 * Sets the flag includeNonDeliveringSupplier.
	 * The flag indicates if non-delivering suppliers (info supplier) contained in the list of suppliers which are checked by the permission.
	 *
	 * @return
	 */
	@Override
    public void setIncludeNonDeliveringSupplier( boolean includeNonDeliveringSupplier ) {
		this.includeNonDeliveringSupplier = includeNonDeliveringSupplier;
	}

	/**
	 * Returns whether non-delivering suppliers (info supplier) contained in the list of suppliers which are checked by the permission or not.
	 *
	 * @return whether non-delivering suppliers (info supplier) contained in the list of suppliers which are checked by the permission or not.
	 */
	@Override
    @Column( name = "`includeNonDeliveringSupplier`", nullable = false )
	public boolean isIncludeNonDeliveringSupplier() {
		return includeNonDeliveringSupplier;
	}

	@Override
     public void setRight( String right ) {
        this.right = right;
    }

	@Override
    @Column( name = "right", nullable = false )
    public String getRight() {
        return this.right;
    }


}
