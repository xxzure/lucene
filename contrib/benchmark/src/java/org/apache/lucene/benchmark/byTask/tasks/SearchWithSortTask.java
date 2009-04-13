package org.apache.lucene.benchmark.byTask.tasks;
/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.lucene.benchmark.byTask.PerfRunData;
import org.apache.lucene.benchmark.byTask.feeds.QueryMaker;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;

/**
 * Does sort search on specified field.
 * 
 */
public class SearchWithSortTask extends ReadTask {

  private boolean doScore = true;
  private boolean doMaxScore = true;
  private Sort sort;

  public SearchWithSortTask(PerfRunData runData) {
    super(runData);
  }

  /**
   * SortFields: field:type,field:type[,noscore][,nomaxscore]
   *
   * If noscore is present, then we turn off score tracking
   * in {@link org.apache.lucene.search.TopFieldCollector}.
   * If nomaxscore is present, then we turn off maxScore tracking
   * in {@link org.apache.lucene.search.TopFieldCollector}.
   * 
   * name,byline:int,subject:auto
   * 
   */
  public void setParams(String sortField) {
    super.setParams(sortField);
    String[] fields = sortField.split(",");
    SortField[] sortFields = new SortField[fields.length];
    int upto = 0;
    for (int i = 0; i < fields.length; i++) {
      String field = fields[i];
      SortField sortField0;
      if (field.equals("doc")) {
        sortField0 = SortField.FIELD_DOC;
      } else if (field.equals("noscore")) {
        doScore = false;
        continue;
      } else if (field.equals("nomaxscore")) {
        doMaxScore = false;
        continue;
      } else {
        int index = field.lastIndexOf(":");
        String fieldName;
        String typeString;
        if (index != -1) {
          fieldName = field.substring(0, index);
          typeString = field.substring(1+index, field.length());
        } else {
          typeString = "auto";
          fieldName = field;
        }
        int type = getType(typeString);
        sortField0 = new SortField(fieldName, type);
      }
      sortFields[upto++] = sortField0;
    }

    if (upto < sortFields.length) {
      SortField[] newSortFields = new SortField[upto];
      System.arraycopy(sortFields, 0, newSortFields, 0, upto);
      sortFields = newSortFields;
    }
    this.sort = new Sort(sortFields);
  }

  private int getType(String typeString) {
    int type;
    if (typeString.equals("float")) {
      type = SortField.FLOAT;
    } else if (typeString.equals("int")) {
      type = SortField.INT;
    } else if (typeString.equals("string")) {
      type = SortField.STRING;
    } else if (typeString.equals("string_val")) {
      type = SortField.STRING_VAL;
    } else {
      type = SortField.AUTO;
    }
    return type;
  }

  public boolean supportsParams() {
    return true;
  }

  public QueryMaker getQueryMaker() {
    return getRunData().getQueryMaker(this);
  }

  public boolean withRetrieve() {
    return false;
  }

  public boolean withSearch() {
    return true;
  }

  public boolean withTraverse() {
    return false;
  }

  public boolean withWarm() {
    return false;
  }

  public boolean withScore() {
    return doScore;
  }

  public boolean withMaxScore() {
    return doMaxScore;
  }
  
  public Sort getSort() {
    if (sort == null) {
      throw new IllegalStateException("No sort field was set");
    }
    return sort;
  }

}
