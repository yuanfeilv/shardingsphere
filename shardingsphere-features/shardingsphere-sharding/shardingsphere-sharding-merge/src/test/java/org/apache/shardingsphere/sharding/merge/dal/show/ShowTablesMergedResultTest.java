/*
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

package org.apache.shardingsphere.sharding.merge.dal.show;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.infra.metadata.schema.model.physical.PhysicalTableMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.physical.PhysicalSchemaMetaData;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.executor.sql.QueryResult;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShowTablesMergedResultTest {
    
    private ShardingRule shardingRule;
    
    private PhysicalSchemaMetaData schemaMetaData;
    
    @Before
    public void setUp() {
        shardingRule = createShardingRule();
        schemaMetaData = createSchemaMetaData();
    }
    
    private ShardingRule createShardingRule() {
        ShardingTableRuleConfiguration tableRuleConfig = new ShardingTableRuleConfiguration("table", "ds.table_${0..2}");
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTables().add(tableRuleConfig);
        return new ShardingRule(shardingRuleConfig, Lists.newArrayList("ds"));
    }
    
    private PhysicalSchemaMetaData createSchemaMetaData() {
        Map<String, PhysicalTableMetaData> tableMetaDataMap = new HashMap<>(1, 1);
        tableMetaDataMap.put("table", new PhysicalTableMetaData(Collections.emptyList(), Collections.emptyList()));
        return new PhysicalSchemaMetaData(tableMetaDataMap);
    }
    
    private QueryResult createQueryResult(final String value) throws SQLException {
        QueryResult result = mock(QueryResult.class);
        when(result.next()).thenReturn(true, false);
        when(result.getValue(1, Object.class)).thenReturn(value);
        when(result.getColumnCount()).thenReturn(1);
        return result;
    }
    
    @Test
    public void assertNextForEmptyQueryResult() throws SQLException {
        LogicTablesMergedResult actual = new LogicTablesMergedResult(shardingRule, mock(SQLStatementContext.class), schemaMetaData, Collections.emptyList());
        assertFalse(actual.next());
    }
    
    @Test
    public void assertNextForActualTableNameInTableRule() throws SQLException {
        LogicTablesMergedResult actual = new LogicTablesMergedResult(shardingRule, mock(SQLStatementContext.class), schemaMetaData, Collections.singletonList(createQueryResult("table_0")));
        assertTrue(actual.next());
    }
}
