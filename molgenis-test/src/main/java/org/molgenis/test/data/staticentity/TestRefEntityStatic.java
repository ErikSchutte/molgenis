package org.molgenis.test.data.staticentity;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.StaticEntity;

import static org.molgenis.test.data.EntityTestHarness.ATTR_REF_ID;
import static org.molgenis.test.data.EntityTestHarness.ATTR_REF_STRING;

public class TestRefEntityStatic extends StaticEntity
{
	public TestRefEntityStatic(Entity entity)
	{
		super(entity);
	}

	public TestRefEntityStatic(EntityMetaData entityMeta)
	{
		super(entityMeta);
	}

	public TestRefEntityStatic(String id, EntityMetaData entityMeta)
	{
		super(entityMeta);
		setId(id);
	}

	public void setId(String id)
	{
		set(ATTR_REF_ID, id);
	}

	public String getId()
	{
		return getString(ATTR_REF_ID);
	}

	public void setRefString(String refString)
	{
		set(ATTR_REF_STRING, refString);
	}

	public String getRefString()
	{
		return getString(ATTR_REF_STRING);
	}

	@Override
	public String toString()
	{
		return "RefEntity[" + getId() + "]";
	}
}
