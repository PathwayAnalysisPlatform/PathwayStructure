/*
 * Copyright 2017 Luis Francisco Hernández Sánchez.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package no.uib.pathwayquery;

import java.io.UnsupportedEncodingException;
import no.uib.Model.BiMapShortToByteArray;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class test {

    public static void main(String args[]) throws UnsupportedEncodingException {
        BiMapShortToByteArray map;
        map = new BiMapShortToByteArray(12);

        map.put("P00519");
        map.put("P31749");
        map.put("P31749");
        map.put("P11274");
        map.put("P22681");
        map.put("P16220");
        map.put("P46109");
        map.put("P27361");
        map.put("Q9UQC2");
        map.put("Q15759");
        map.put("O15530");
        map.put("P62753");

        short sho = map.getShort("P11274");
        String str = map.getString(sho);
        System.out.println(sho);
        System.out.println(str);
        System.out.println(map.containsId(str));


    }
}
