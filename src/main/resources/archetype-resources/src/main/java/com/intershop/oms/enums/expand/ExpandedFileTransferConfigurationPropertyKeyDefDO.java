/**
 *
 */
package com.intershop.oms.enums.expand;

import java.util.EnumSet;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;

import bakery.persistence.annotation.ExpandedEnum;
import bakery.persistence.dataobject.configuration.connections.ParameterTypeDefDO;
import bakery.persistence.dataobject.file.in.FileTransferConfigurationPropertyKeyDefDO;
import bakery.persistence.expand.FileTransferConfigurationPropertyKeyDefDOEnumInterface;

/**
 * @author Manivarnan Muthusamy
 *
 */
@ExpandedEnum(FileTransferConfigurationPropertyKeyDefDO.class)
public enum ExpandedFileTransferConfigurationPropertyKeyDefDO
                implements FileTransferConfigurationPropertyKeyDefDOEnumInterface
{

	// start with 10000 to avoid conflicts with TransferConfigurationPropertyKeyDefDO
	// the name must be unique across both classes
	// values with negative id are meant as syntax example and are ignored (won't get persisted within the db)

	EXAMPLE(-999, "ExampleDescription", ParameterTypeDefDO.STRING);

    private final Integer id;
    private final String description;
    private ParameterTypeDefDO parameterTypeDefDO;
    private Integer parameterTypeDefRef;

    ExpandedFileTransferConfigurationPropertyKeyDefDO(Integer id, String description,
                    ParameterTypeDefDO parameterTypeDefDO)
    {
        this.id = id;
        this.description = description;
        this.parameterTypeDefDO = parameterTypeDefDO;
        this.parameterTypeDefRef = parameterTypeDefDO.getId();
    }

    @Override
	@Id
    public Integer getId()
    {
        return this.id;
    }

    @Override
	@Column( name = "`description`" )
    public String getDescription()
    {
        return this.description;
    }

    @Override
	@Transient
    public ParameterTypeDefDO getParameterTypeDefDO()
    {
        return this.parameterTypeDefDO;
    }

    @Override
	@Column( name = "`parameterTypeDefRef`" )
    public Integer getParameterTypeDefRef()
    {
        return this.parameterTypeDefRef;
    }

    @Override
	@Transient
    public String getFieldName()
    {
        return this.name();
    }

    @Transient
    public final EnumSet<ExpandedFileTransferConfigurationPropertyKeyDefDO> getExpandedEnums()
    {
        return EnumSet.allOf(ExpandedFileTransferConfigurationPropertyKeyDefDO.class);
    }
}
