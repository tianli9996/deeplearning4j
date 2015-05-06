/*
 *
 *  * Copyright 2015 Skymind,Inc.
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *        http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package org.deeplearning4j.spark.models.embeddings.word2vec;

import org.deeplearning4j.berkeley.Triple;
import org.deeplearning4j.models.embeddings.inmemory.InMemoryLookupTable;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.Serializable;
import java.util.*;

/**
 * @author Adam Gibson
 */
public class Word2VecChange implements Serializable {
    private Map<Integer,Set<INDArray>> changes = new HashMap<>();

    public Word2VecChange(List<Triple<Integer,Integer,Integer>> counterMap,Word2VecParam param) {
        Iterator<Triple<Integer,Integer,Integer>> iter = counterMap.iterator();
        while(iter.hasNext()) {
            Triple<Integer,Integer,Integer> next = iter.next();
            Set<INDArray> changes = this.changes.get(next.getFirst());
            if(changes == null) {
                changes = new HashSet<>();
                this.changes.put(next.getFirst(),changes);
            }

            changes.add(param.getWeights().getSyn1().slice(next.getSecond()));

        }
    }

    public Map<Integer, Set<INDArray>> getChanges() {
        return changes;
    }

    public void setChanges(Map<Integer, Set<INDArray>> changes) {
        this.changes = changes;
    }

    /**
     * Take the changes and apply them
     * to the given table
     * @param table the memory lookup table
     *              to apply the changes to
     */
    public void apply(InMemoryLookupTable table) {

        for(Integer i : changes.keySet()) {
            Set<INDArray> changes = this.changes.get(i);
            INDArray toChange = table.getSyn0().slice(i);
            for(INDArray syn1 : changes)
                Nd4j.getBlasWrapper().axpy(1,syn1,toChange);
        }
    }
}
