#!/usr/bin/env python3
"""
调用链 JSON 数据提取和可视化工具
用于从 analysis-output.json 中提取和展示调用链信息
"""

import json
import sys
from typing import Dict, List

def load_analysis_result(file_path: str) -> dict:
    """加载分析结果 JSON"""
    with open(file_path, 'r', encoding='utf-8') as f:
        return json.load(f)

def build_lookup_maps(data: dict) -> tuple:
    """构建快速查找映射表"""
    method_map = {m['id']: m for m in data['methods']}
    class_map = {c['id']: c for c in data['classes']}
    service_map = {s['id']: s for s in data['services']}
    return method_map, class_map, service_map

def print_call_chain_detail(chain: dict, method_map: Dict, class_map: Dict, service_map: Dict):
    """打印单条调用链的详细信息"""
    print(f"\n{'='*80}")
    print(f"调用链 ID: {chain['id']}")
    print(f"{'='*80}")

    # 入口点信息
    if chain.get('entryPoint'):
        entry = chain['entryPoint']
        print(f"\n📍 入口点:")
        print(f"   HTTP: {entry.get('httpEndpoint', 'N/A')}")

        method = method_map.get(entry['methodId'])
        cls = class_map.get(entry['classId'])
        service = service_map.get(entry['serviceId'])

        if method and cls and service:
            print(f"   方法: {cls['qualifiedName']}.{method['methodName']}()")
            print(f"   服务: {service['name']} ({service['artifactId']})")

    # 调用路径
    print(f"\n🔗 调用路径:")
    for i, node in enumerate(chain['chain']):
        method = method_map.get(node['methodId'])
        cls = class_map.get(node['classId'])

        if method and cls:
            indent = '  ' * (node['level'] + 1)
            arrow = '└─' if i == len(chain['chain']) - 1 else '├─'

            print(f"{indent}{arrow} [Level {node['level']}] {cls['className']}.{method['methodName']}()")

            if node.get('httpEndpoint'):
                print(f"{indent}   HTTP: {node['httpEndpoint']}")

            if node.get('callType'):
                print(f"{indent}   类型: {node['callType']}")

    # 统计信息
    print(f"\n📊 统计:")
    print(f"   最大深度: {chain['maxDepth']}")
    print(f"   涉及服务数: {len(chain['involvedServices'])}")
    print(f"   跨服务调用: {'是' if chain['crossService'] else '否'}")

def print_all_chains_summary(data: dict, method_map: Dict, class_map: Dict):
    """打印所有调用链的摘要"""
    print(f"\n{'='*80}")
    print(f"调用链汇总")
    print(f"{'='*80}")

    for i, chain in enumerate(data['callChains'], 1):
        entry = chain.get('entryPoint', {})
        method = method_map.get(entry.get('methodId', ''))
        cls = class_map.get(entry.get('classId', ''))

        print(f"\n{i}. {entry.get('httpEndpoint', 'N/A')}")
        if method and cls:
            print(f"   → {cls['className']}.{method['methodName']}()")
        print(f"   深度: {chain['maxDepth']} | 跨服务: {'✓' if chain['crossService'] else '✗'}")

def export_to_neo4j_cypher(data: dict, output_file: str):
    """导出为 Neo4j Cypher 查询语句"""
    method_map, class_map, service_map = build_lookup_maps(data)

    with open(output_file, 'w', encoding='utf-8') as f:
        f.write("// Neo4j Cypher 导入脚本\n")
        f.write("// 由 Call Chain Analyzer 生成\n\n")

        # 创建服务节点
        f.write("// 1. 创建服务节点\n")
        for service in data['services']:
            f.write(f"CREATE (s_{service['id'][:8]}:Service {{\n")
            f.write(f"  id: '{service['id']}',\n")
            f.write(f"  name: '{service['name']}',\n")
            f.write(f"  artifactId: '{service['artifactId']}'\n")
            f.write(f"}});\n\n")

        # 创建类节点
        f.write("// 2. 创建类节点\n")
        for cls in data['classes']:
            f.write(f"CREATE (c_{cls['id'][:8]}:Class {{\n")
            f.write(f"  id: '{cls['id']}',\n")
            f.write(f"  name: '{cls['className']}',\n")
            f.write(f"  qualifiedName: '{cls['qualifiedName']}',\n")
            f.write(f"  type: '{cls['type']}'\n")
            f.write(f"}});\n\n")

        # 创建方法节点
        f.write("// 3. 创建方法节点\n")
        for method in data['methods']:
            f.write(f"CREATE (m_{method['id'][:8]}:Method {{\n")
            f.write(f"  id: '{method['id']}',\n")
            f.write(f"  name: '{method['methodName']}',\n")
            f.write(f"  signature: '{method['signature']}'\n")
            f.write(f"}});\n\n")

        # 创建方法调用关系
        f.write("// 4. 创建方法调用关系\n")
        for call in data['methodCalls']:
            if call.get('targetMethodId'):
                f.write(f"MATCH (m1:Method {{id: '{call['sourceMethodId']}'}})\n")
                f.write(f"MATCH (m2:Method {{id: '{call['targetMethodId']}'}})\n")
                f.write(f"CREATE (m1)-[:CALLS {{\n")
                f.write(f"  callType: '{call['callType']}',\n")
                f.write(f"  crossService: {str(call['crossService']).lower()}\n")
                f.write(f"}}]->(m2);\n\n")

        print(f"Neo4j Cypher 脚本已导出到: {output_file}")

def main():
    if len(sys.argv) < 2:
        print("用法: python3 view_call_chains.py <analysis-output.json> [--cypher output.cypher]")
        sys.exit(1)

    input_file = sys.argv[1]

    # 加载数据
    print(f"正在加载: {input_file}")
    data = load_analysis_result(input_file)

    # 构建查找映射
    method_map, class_map, service_map = build_lookup_maps(data)

    # 打印摘要
    print_all_chains_summary(data, method_map, class_map)

    # 打印详细信息
    for chain in data['callChains']:
        print_call_chain_detail(chain, method_map, class_map, service_map)

    # 导出 Cypher（可选）
    if '--cypher' in sys.argv:
        cypher_index = sys.argv.index('--cypher')
        if cypher_index + 1 < len(sys.argv):
            output_file = sys.argv[cypher_index + 1]
            export_to_neo4j_cypher(data, output_file)

if __name__ == '__main__':
    main()
