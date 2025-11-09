// Neo4j Cypher 导入脚本
// 由 Call Chain Analyzer 生成

// 1. 创建服务节点
CREATE (s_d286b041:Service {
  id: 'd286b041-9d3e-4fef-bd42-ec83c1300f1e',
  name: 'User Service',
  artifactId: 'user-service'
});

// 2. 创建类节点
CREATE (c_935f9b1c:Class {
  id: '935f9b1c-3c94-4b96-8d4c-60f95e51748a',
  name: 'UserService',
  qualifiedName: 'com.example.user.service.UserService',
  type: 'SERVICE'
});

CREATE (c_403987b2:Class {
  id: '403987b2-f56c-49e7-ba1a-a5900404662b',
  name: 'User',
  qualifiedName: 'com.example.user.model.User',
  type: 'PLAIN_CLASS'
});

CREATE (c_d86558cd:Class {
  id: 'd86558cd-43e4-49d0-b968-4c43fcdc2434',
  name: 'UserController',
  qualifiedName: 'com.example.user.controller.UserController',
  type: 'CONTROLLER'
});

// 3. 创建方法节点
CREATE (m_68da47c0:Method {
  id: '68da47c0-a7fb-4cab-b46c-f8c85117a903',
  name: 'findUserById',
  signature: 'User findUserById(Long)'
});

CREATE (m_cc6d3608:Method {
  id: 'cc6d3608-ad32-4795-8008-9f4fe1417ca8',
  name: 'createUser',
  signature: 'User createUser(User)'
});

CREATE (m_2bead354:Method {
  id: '2bead354-29a6-488f-abe2-f8b95e352146',
  name: 'deleteUser',
  signature: 'void deleteUser(Long)'
});

CREATE (m_3b7a1c6f:Method {
  id: '3b7a1c6f-626b-4ae0-8b8f-224022ab756a',
  name: 'getId',
  signature: 'Long getId()'
});

CREATE (m_6cee9262:Method {
  id: '6cee9262-1247-436d-a1eb-7455b2a5d8eb',
  name: 'setId',
  signature: 'void setId(Long)'
});

CREATE (m_48318305:Method {
  id: '48318305-a3fb-4e9b-b711-93ddd8352ed2',
  name: 'getName',
  signature: 'String getName()'
});

CREATE (m_05843b12:Method {
  id: '05843b12-d37c-436a-901a-3d7cba8f835a',
  name: 'setName',
  signature: 'void setName(String)'
});

CREATE (m_3c0cd47a:Method {
  id: '3c0cd47a-bbce-489e-b1f3-0187017ed953',
  name: 'getEmail',
  signature: 'String getEmail()'
});

CREATE (m_bd84c155:Method {
  id: 'bd84c155-7c5a-41e7-9311-5e42e46c17b7',
  name: 'setEmail',
  signature: 'void setEmail(String)'
});

CREATE (m_53b60bfc:Method {
  id: '53b60bfc-7294-4c38-9d07-1c9c7184e53e',
  name: 'getUser',
  signature: 'User getUser(Long)'
});

CREATE (m_37bcd7d1:Method {
  id: '37bcd7d1-29e0-4e67-b82e-923c5229b7e0',
  name: 'createUser',
  signature: 'User createUser(User)'
});

CREATE (m_75394f5e:Method {
  id: '75394f5e-2c80-4d20-ac22-8af9426c18c9',
  name: 'deleteUser',
  signature: 'void deleteUser(Long)'
});

// 4. 创建方法调用关系
MATCH (m1:Method {id: '53b60bfc-7294-4c38-9d07-1c9c7184e53e'})
MATCH (m2:Method {id: '68da47c0-a7fb-4cab-b46c-f8c85117a903'})
CREATE (m1)-[:CALLS {
  callType: 'INTERNAL_METHOD_CALL',
  crossService: false
}]->(m2);

MATCH (m1:Method {id: '37bcd7d1-29e0-4e67-b82e-923c5229b7e0'})
MATCH (m2:Method {id: 'cc6d3608-ad32-4795-8008-9f4fe1417ca8'})
CREATE (m1)-[:CALLS {
  callType: 'INTERNAL_METHOD_CALL',
  crossService: false
}]->(m2);

MATCH (m1:Method {id: '75394f5e-2c80-4d20-ac22-8af9426c18c9'})
MATCH (m2:Method {id: '2bead354-29a6-488f-abe2-f8b95e352146'})
CREATE (m1)-[:CALLS {
  callType: 'INTERNAL_METHOD_CALL',
  crossService: false
}]->(m2);

