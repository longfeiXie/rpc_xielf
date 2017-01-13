package com.epower.rpc.util.seralizable.kryo;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import com.esotericsoftware.kryo.Kryo;

final class PooledKryoFactory extends BasePooledObjectFactory<Kryo> {

	@Override
	public Kryo create() throws Exception {
		return new Kryo();
	}

	@Override
	public PooledObject<Kryo> wrap(Kryo kryo) {
		return new DefaultPooledObject<Kryo>(kryo);
	}
}