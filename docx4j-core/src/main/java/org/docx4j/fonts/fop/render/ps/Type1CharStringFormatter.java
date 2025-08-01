/* NOTICE: This file has been changed by Plutext Pty Ltd for use in docx4j.
 * The package name has been changed; there may also be other changes.
 * 
 * This notice is included to meet the condition in clause 4(b) of the License. 
 */
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.docx4j.fonts.fop.render.ps;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.List;

import org.apache.fontbox.cff.CharStringCommand;

/**
 * This class represents a formatter for CharString commands of a Type1 font.
 * author Villu Ruusmann
 * @version $Revision: 1.0 $
 */
public class Type1CharStringFormatter {

    private ByteArrayOutputStream output;

    /**
     * Formats the given command sequence to a byte array.
     * @param sequence the given command sequence
     * @return the formatted seuqence as byte array
     */
    public byte[] format(List<Object> sequence) {
        output = new ByteArrayOutputStream();

        for (Object object : sequence) {
            if (object instanceof CharStringCommand) {
                writeCommand((CharStringCommand) object);
            } else if (object instanceof Number) {
                writeNumber(((Number) object).intValue());
            } else {
                throw new IllegalArgumentException();
            }
        }
        return output.toByteArray();
    }

    private void writeCommand(CharStringCommand command) {
        int[] value = getValue(command);
        for (int aValue : value) {
            output.write(aValue);
        }
    }

    private int[] getValue(CharStringCommand command) {
        CharStringCommand.Type1KeyWord keyWord = command.getType1KeyWord();
        if (keyWord == null) {
            return new int[0];
        }
        CharStringCommand.Key key = CharStringCommand.Key.valueOf(keyWord.name());
        try {
            Field f = key.getClass().getDeclaredField("hashValue");
            f.setAccessible(true);
            int value = (int) f.get(key);
            return new int[] {value};
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeNumber(Integer number) {
        int value = number;
        if (value >= -107 && value <= 107) {
            output.write(value + 139);
        } else if (value >= 108 && value <= 1131) {
            int b1 = (value - 108) % 256;
            int b0 = (value - 108 - b1) / 256 + 247;
            output.write(b0);
            output.write(b1);
        } else if (value >= -1131 && value <= -108) {
            int b1 = -((value + 108) % 256);
            int b0 = -((value + 108 + b1) / 256 - 251);
            output.write(b0);
            output.write(b1);
        } else {
            int b1 = value >>> 24 & 0xff;
            int b2 = value >>> 16 & 0xff;
            int b3 = value >>> 8 & 0xff;
            int b4 = value >>> 0 & 0xff;
            output.write(255);
            output.write(b1);
            output.write(b2);
            output.write(b3);
            output.write(b4);
        }
    }
}
