package com.epower.rpc.util.seralizable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Serialization {

	public void serialize(final OutputStream out, final Object message) throws IOException;
	
	public Object deserialize(final InputStream in) throws IOException;
}
