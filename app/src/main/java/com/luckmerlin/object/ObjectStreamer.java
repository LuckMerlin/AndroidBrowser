package com.luckmerlin.object;

import com.luckmerlin.core.Matcher;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

public class ObjectStreamer {
    private ByteOutputStream mOutputStream;
    private ByteInputStream mInputStream;

    public boolean write(List<?extends Serializable> objects, Matcher<byte[]> matcher) throws IOException {
        if (null!=objects&&objects.size()>0&&null!=matcher){
            for (Serializable serial:objects) {
                if (null==matcher.match(write(serial))){
                    break;
                }
            }
            return true;
        }
        return false;
    }

    public final byte[] write(Serializable object) throws IOException {
        if (null==object){
            return null;
        }
        ByteOutputStream outputStream=mOutputStream;
        outputStream=null!=outputStream?outputStream:(mOutputStream=new ByteOutputStream());
        outputStream.reset();
        ObjectOutputStream objectOutputStream=new ObjectOutputStream(outputStream);
        objectOutputStream.writeObject(object);
        return outputStream.toByteArray();
    }

    public Object read(byte[] bytes,int offset) throws IOException, ClassNotFoundException {
        ByteInputStream inputStream=mInputStream;
        inputStream=null!=inputStream?inputStream:(mInputStream=new ByteInputStream());
        inputStream.reload(bytes,offset);
        return new ObjectInputStream(inputStream).readObject();
    }

    private static class ByteInputStream extends InputStream{

        protected byte buf[];

        protected int pos;

        protected int mark = 0;

        protected int count;

        public ByteInputStream() {
            this(null);
        }

        public ByteInputStream(byte buf[]) {
            this.buf = buf;
            this.pos = 0;
            this.count = null!=buf?buf.length:0;
        }

        public ByteInputStream(byte buf[], int offset, int length) {
            this.buf = buf;
            this.pos = offset;
            this.count = Math.min(offset + length, buf.length);
            this.mark = offset;
        }

        public synchronized ByteInputStream reload(byte buffer[],int offset){
            return null!=buffer?reload(buffer,offset,buffer.length):this;
        }

        public synchronized ByteInputStream reload(byte buffer[],int offset,int length){
            this.buf = buffer;
            this.pos = offset;
            this.count = Math.min(offset + length, buf.length);
            this.mark = offset;
            return this;
        }

        public synchronized int read() {
            return (pos < count) ? (buf[pos++] & 0xff) : -1;
        }

        public synchronized int read(byte b[], int off, int len) {
            if (b == null) {
                throw new NullPointerException();
            } else if (off < 0 || len < 0 || len > b.length - off) {
                throw new IndexOutOfBoundsException();
            }

            if (pos >= count) {
                return -1;
            }

            int avail = count - pos;
            if (len > avail) {
                len = avail;
            }
            if (len <= 0) {
                return 0;
            }
            System.arraycopy(buf, pos, b, off, len);
            pos += len;
            return len;
        }

        public synchronized long skip(long n) {
            long k = count - pos;
            if (n < k) {
                k = n < 0 ? 0 : n;
            }

            pos += k;
            return k;
        }

        public synchronized int available() {
            return count - pos;
        }

        public boolean markSupported() {
            return true;
        }

        public void mark(int readAheadLimit) {
            mark = pos;
        }

        public synchronized void reset() {
            pos = mark;
        }

        public void close() throws IOException {
        }

    }

    private static class ByteOutputStream extends OutputStream{
        protected byte buf[];

        protected int count;

        public ByteOutputStream() {
            this(32);
        }

        public ByteOutputStream(int size) {
            if (size < 0) {
                throw new IllegalArgumentException("Negative initial size: "
                        + size);
            }
            buf = new byte[size];
        }

        private void ensureCapacity(int minCapacity) {
            // overflow-conscious code
            if (minCapacity - buf.length > 0)
                grow(minCapacity);
        }

        private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

        private void grow(int minCapacity) {
            // overflow-conscious code
            int oldCapacity = buf.length;
            int newCapacity = oldCapacity << 1;
            if (newCapacity - minCapacity < 0)
                newCapacity = minCapacity;
            if (newCapacity - MAX_ARRAY_SIZE > 0)
                newCapacity = hugeCapacity(minCapacity);
            buf = Arrays.copyOf(buf, newCapacity);
        }

        private static int hugeCapacity(int minCapacity) {
            if (minCapacity < 0) // overflow
                throw new OutOfMemoryError();
            return (minCapacity > MAX_ARRAY_SIZE) ?
                    Integer.MAX_VALUE :
                    MAX_ARRAY_SIZE;
        }

        public synchronized void write(int b) {
            ensureCapacity(count + 1);
            buf[count] = (byte) b;
            count += 1;
        }

        public synchronized void write(byte b[], int off, int len) {
            if ((off < 0) || (off > b.length) || (len < 0) ||
                    ((off + len) - b.length > 0)) {
                throw new IndexOutOfBoundsException();
            }
            ensureCapacity(count + len);
            System.arraycopy(b, off, buf, count, len);
            count += len;
        }

        public synchronized void writeTo(OutputStream out) throws IOException {
            out.write(buf, 0, count);
        }

        public synchronized void reset() {
            count = 0;
        }

        public synchronized byte toByteArray()[] {
            return Arrays.copyOf(buf, count);
        }

        public synchronized int size() {
            return count;
        }

        public synchronized String toString() {
            return new String(buf, 0, count);
        }

        public synchronized String toString(String charsetName)
                throws UnsupportedEncodingException
        {
            return new String(buf, 0, count, charsetName);
        }

        @Deprecated
        public synchronized String toString(int hibyte) {
            return new String(buf, hibyte, 0, count);
        }

        public void close() throws IOException {
        }
    }
}
