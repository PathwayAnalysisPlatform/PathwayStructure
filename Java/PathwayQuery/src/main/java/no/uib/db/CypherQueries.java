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
package no.uib.db;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class CypherQueries {

    public enum Queries {
        getProteinsByPsiMod {
            public String toString() {
                return "MATCH (re:ReferenceEntity)<-[:referenceEntity]-(ewas:EntityWithAccessionedSequence)-[:hasModifiedResidue]->(mr)-[:psiMod]->(mod)\n"
                        + "WHERE mod.identifier IN {modList} AND ewas.speciesName = \"Homo sapiens\"\n"
                        + "RETURN DISTINCT re.identifier as protein";
            }
        },
        getCountAllPTMs {
            public String toString() {
                return "MATCH path = (re:ReferenceEntity)<-[:referenceEntity]-(ewas:EntityWithAccessionedSequence)-[:hasModifiedResidue]->(mr)-[:psiMod]->(mod) \n"
                        + "WHERE ewas.speciesName = \"Homo sapiens\" RETURN count(DISTINCT re), mod.identifier, mod.name";
            }
        }
    }
}
