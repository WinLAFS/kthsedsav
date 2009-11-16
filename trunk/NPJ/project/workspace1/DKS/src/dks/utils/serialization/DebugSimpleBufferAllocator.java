/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package dks.utils.serialization;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.StreamCorruptedException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.apache.log4j.Logger;
import org.apache.mina.core.buffer.AbstractIoBuffer;
import org.apache.mina.core.buffer.BufferDataException;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.buffer.IoBufferAllocator;

//import dks.niche.interfaces.NicheAsynchronousInterface;



/**
 * A simplistic {@link IoBufferAllocator} which simply allocates a new
 * buffer every time.
 *
 * @author The Apache MINA Project (dev@mina.apache.org)
 * @version $Rev: 671827 $, $Date: 2008-06-26 10:49:48 +0200 (Thu, 26 Jun 2008) $
 */
public class DebugSimpleBufferAllocator implements IoBufferAllocator {

	Logger logger;
	
	public DebugSimpleBufferAllocator(Logger logger) {
		this.logger = logger;
		
	}
    public IoBuffer allocate(int capacity, boolean direct) {
        return wrap(allocateNioBuffer(capacity, direct));
    }
    
    public ByteBuffer allocateNioBuffer(int capacity, boolean direct) {
        ByteBuffer nioBuffer;
        if (direct) {
            nioBuffer = ByteBuffer.allocateDirect(capacity);
        } else {
            nioBuffer = ByteBuffer.allocate(capacity);
        }
        return nioBuffer;
    }

    public IoBuffer wrap(ByteBuffer nioBuffer) {
        return new SimpleBuffer(nioBuffer);
    }

    public void dispose() {
    }

    private class SimpleBuffer extends AbstractIoBuffer {
        private class SimpleBufferPosition implements BufferPosition {
        	private AbstractIoBuffer aiob;
        	public SimpleBufferPosition(AbstractIoBuffer aiobIn) {
        		aiob = aiobIn;
        	}
        	public int getPosition() { return(aiob.position()); }
        }

        private ByteBuffer buf;
        SimpleBufferPosition sbp;
        protected SimpleBuffer(ByteBuffer buf) {
            super(DebugSimpleBufferAllocator.this, buf.capacity());
            this.buf = buf;
            buf.order(ByteOrder.BIG_ENDIAN);
            sbp = new SimpleBufferPosition(this);
        }

        protected SimpleBuffer(SimpleBuffer parent, ByteBuffer buf) {
            super(parent);
            this.buf = buf;
        }

        @Override
        public ByteBuffer buf() {
            return buf;
        }
        
        @Override
        protected void buf(ByteBuffer buf) {
            this.buf = buf;
        }

        @Override
        protected IoBuffer duplicate0() {
            return new SimpleBuffer(this, this.buf.duplicate());
        }

        @Override
        protected IoBuffer slice0() {
            return new SimpleBuffer(this, this.buf.slice());
        }

        @Override
        protected IoBuffer asReadOnlyBuffer0() {
            return new SimpleBuffer(this, this.buf.asReadOnlyBuffer());
        }

        @Override
        public byte[] array() {
            return buf.array();
        }

        @Override
        public int arrayOffset() {
            return buf.arrayOffset();
        }

        @Override
        public boolean hasArray() {
            return buf.hasArray();
        }

        @Override
        public void free() {
        }

       @Override
        public IoBuffer putObject(Object o) {
            int oldPos = position();
            skip(4); // Make a room for the length field.
            try {
                ObjectOutputStream out = 
                	new DebugObjectOutputStream(logger, asOutputStream(), sbp) {
                    @Override
                    protected void writeClassDescriptor(ObjectStreamClass desc)
                            throws IOException {
                        if (desc.forClass().isPrimitive()) {
                            write(0);
                            super.writeClassDescriptor(desc);
                        } else {
                            write(1);
                            writeUTF(desc.getName());
                        }
                    }
                };
                out.writeObject(o);
                out.flush();
            } catch (IOException e) {
                throw new BufferDataException(e);
            }

            // Fill the length field
            int newPos = position();
            position(oldPos);
            putInt(newPos - oldPos - 4);
            position(newPos);
            return this;
        }

        @Override
        public Object getObject(final ClassLoader classLoader)
                throws ClassNotFoundException {
            if (!prefixedDataAvailable(4)) {
                throw new BufferUnderflowException();
            }

            int length = getInt();
            if (length <= 4) {
                throw new BufferDataException(
                        "Object length should be greater than 4: " + length);
            }

            int oldLimit = limit();
            limit(position() + length);
            try {
                ObjectInputStream in = new DebugObjectInputStream(logger, asInputStream()) {
                    @Override
                    protected ObjectStreamClass readClassDescriptor()
                            throws IOException, ClassNotFoundException {
                        int type = read();
                        if (type < 0) {
                            throw new EOFException();
                        }
                        switch (type) {
                        case 0: // Primitive types
                            return super.readClassDescriptor();
                        case 1: // Non-primitive types
                            String className = readUTF();
                            Class<?> clazz = Class.forName(className, true,
                                    classLoader);
                            return ObjectStreamClass.lookup(clazz);
                        default:
                            throw new StreamCorruptedException(
                                    "Unexpected class descriptor type: " + type);
                        }
                    }

                    @Override
                    protected Class<?> resolveClass(ObjectStreamClass desc)
                            throws IOException, ClassNotFoundException {
                        String name = desc.getName();
                        try {
                            return Class.forName(name, false, classLoader);
                        } catch (ClassNotFoundException ex) {
                            return super.resolveClass(desc);
                        }
                    }
                };
                return in.readObject();
            } catch (IOException e) {
                throw new BufferDataException(e);
            } finally {
                limit(oldLimit);
            }
        }


    }
}
