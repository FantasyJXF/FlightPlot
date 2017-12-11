package me.drton.jmavlib.log.px4;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.*;

/**
 * User: ton Date: 03.06.13 Time: 14:35
 */
public class PX4LogMessageDescription {
    private static Charset charset = Charset.forName("latin1");

    static PX4LogMessageDescription FORMAT = new PX4LogMessageDescription(0x80, 89, "FMT", "BBnNZ",
            new String[]{"Type", "Length", "Name", "Format", "Labels"}); // FMT的消息序号为0x80. log_format_s结构体长度为86，每条日志消息有三个字节的包头长度

    public final int type;
    public final int length;
    public final String name;
    public final String format;
    public final String[] fields;
    public final Map<String, Integer> fieldsMap = new HashMap<String, Integer>();

    public PX4LogMessageDescription(int type, int length, String name, String format, String[] fields) {
        this.type = type;
        this.length = length;
        this.name = name;
        this.format = format;
        this.fields = fields;
    }

    private static String getString(ByteBuffer buffer, int len) {
        byte[] strBuf = new byte[len];
        buffer.get(strBuf);
        String[] p = new String(strBuf, charset).split("\0"); // 字符串结束符
        return p.length > 0 ? p[0] : "";
    }

    public PX4LogMessageDescription(ByteBuffer buffer) {
        type = buffer.get() & 0xFF;
        length = buffer.get() & 0xFF;
        name = getString(buffer, 4);
        format = getString(buffer, 16);
        String fieldsStr = getString(buffer, 64);
        fields = fieldsStr.length() > 0 ? fieldsStr.split(",") : new String[0]; // 取得结结构体中以逗号分隔的字段，放到String[] fields字符串数组中
        if (!"FMT".equals(name)) {    // Workaround for buggy and useless APM "FMT" format
            if (fields.length != format.length()) {
                throw new RuntimeException(String.format("Labels count != format length: name = \"%s\" fields = %s, format = \"%s\"",
                        name, Arrays.asList(fields), format));
            }
            for (int i = 0; i < fields.length; i++) {
                fieldsMap.put(fields[i], i); // 将字符串数组中结构体成员名字放到fieldsMap这个HashMap中
            }
        }
    }

    public PX4LogMessage parseMessage(ByteBuffer buffer) {
        List<Object> data = new ArrayList<Object>(format.length());
        for (char f : format.toCharArray()) { // 将字符串转换成字符数组
            Object v;
            if (f == 'f') {
                v = buffer.getFloat();
            } else if (f == 'q' || f == 'Q') {
                v = buffer.getLong();
            } else if (f == 'i') {
                v = buffer.getInt();
            } else if (f == 'I') {
                v = buffer.getInt() & 0xFFFFFFFFl;
            } else if (f == 'b') {
                v = (int) buffer.get();
            } else if (f == 'B' || f == 'M') {
                v = buffer.get() & 0xFF;
            } else if (f == 'L') {
                v = buffer.getInt() * 1e-7;
            } else if (f == 'h') {
                v = (int) buffer.getShort();
            } else if (f == 'H') {
                v = buffer.getShort() & 0xFFFF;
            } else if (f == 'n') {
                v = getString(buffer, 4);
            } else if (f == 'N') {
                v = getString(buffer, 16);
            } else if (f == 'Z') {
                v = getString(buffer, 64);
            } else if (f == 'c') {
                v = (int) buffer.getShort() * 1e-2;
            } else if (f == 'C') {
                v = (buffer.getShort() & 0xFFFF) * 1e-2;
            } else if (f == 'e') {
                v = buffer.getInt() * 1e-2;
            } else if (f == 'E') {
                v = (buffer.getInt() & 0xFFFFFFFFl) * 1e-2;
            } else {
                throw new RuntimeException("Invalid format char in message " + name + ": " + f);
            }
            data.add(v);// 将解析得到的数据加入data中，data是各种消息的结构体成员数据
        }
        return new PX4LogMessage(this, data); // 描述 + 数据 = 日志消息
    }

    public List<String> getFields() {
        return Arrays.asList(fields);
    }

    @Override
    public String toString() {
        return String.format("PX4LogMessageDescription: type=%s, length=%s, name=%s, format=%s, fields=%s", type,
                length, name, format, Arrays.asList(fields));
    }
}
