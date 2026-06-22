<template>
  <div class="product-manual">
    <!-- 面包屑导航 -->
    <el-breadcrumb separator="/" class="breadcrumb">
      <el-breadcrumb-item>分布式调度</el-breadcrumb-item>
      <el-breadcrumb-item>文档</el-breadcrumb-item>
      <el-breadcrumb-item>产品说明</el-breadcrumb-item>
    </el-breadcrumb>

    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">
        <i class="icon-users"></i>介绍及安装使用说明
      </h1>
    </div>

    <!-- 快速导航 -->
    <p class="nav-intro">
      从以下几个部分了解 WWS 分布式调度工具：
      <el-space wrap>
        <el-tag type="primary" effect="plain" @click="scrollToSection('bg_introduction')" class="nav-tag">背景</el-tag>
        <el-tag type="primary" effect="plain" @click="scrollToSection('vision')" class="nav-tag">目标</el-tag>
        <el-tag type="primary" effect="plain" @click="scrollToSection('architecture')" class="nav-tag">架构</el-tag>
        <el-tag type="primary" effect="plain" @click="scrollToSection('deploy')" class="nav-tag">部署</el-tag>
        <el-tag type="primary" effect="plain" @click="scrollToSection('config_tab')" class="nav-tag">数据库表</el-tag>
        <el-tag type="primary" effect="plain" @click="scrollToSection('fel_expression')" class="nav-tag">EL 表达式</el-tag>
      </el-space>
    </p>

    <!-- 1. 背景 -->
    <el-card id="bg_introduction" class="section-card">
      <template #header>
        <div class="card-header">
          <h2>背景</h2>
          <div class="quick-links">
            <el-tag size="small" @click="scrollToSection('bg_introduction')">背景</el-tag>
            <el-tag size="small" @click="scrollToSection('vision')">目标</el-tag>
            <el-tag size="small" @click="scrollToSection('architecture')">架构</el-tag>
            <el-tag size="small" @click="scrollToSection('deploy')">部署</el-tag>
            <el-tag size="small" @click="scrollToSection('config_tab')">数据库表</el-tag>
            <el-tag size="small" @click="scrollToSection('fel_expression')">EL 表达式</el-tag>
          </div>
        </div>
      </template>
      
      <div class="section-content">
        <h3>1. 云平台迁移项目</h3>
        <p>随着行内云平台应用迁移项目开始实施，外围和后线 OLAP 类系统急需一个能在 x86 平台上运行的，自动化程度较高，且高可用的作业调度工具。</p>
        <p>将应用从小型机迁移到 x86 平台后，应用服务器的数量成倍式的增长，单节点的资源有限，因此作业的调度执行需要支持分布式。</p>
        <p>在云平台项目实施过程中，目标应用的子系统或模块较多，用户希望能有一个集中式管理的统一调度平台，当新增一个子系统，只需将一个新的服务器节点纳入调度平台统一管理，并实现资源隔离的目的。</p>
        
        <el-divider />
        
        <h3>2. MPP 架构数据库在行内的推广</h3>
        <p>2018 年经过总行选型，确定 GBASE 为 MPP 数据库供应商.</p>
        <p>由于 GBASE 产品并没有提供对应的调度工具，根据前期调研情况，在 MPP 产品选型落地后，需要调度工具除支持 Oracle 作业外，还需对分布式数据库有良好支持，在调度作业时能够屏蔽因不同数据库类型而带来的额外操作。</p>
        
        <el-divider />
        
        <h3>3. 中银金科挂牌成立</h3>
        <p>2019 年 6 月 13 日中银金科挂牌，由中行软件中心（上海）转制成立.</p>
        <p>姚总用生动朴实的语言，告诉了我们中银金科公司既不是软件中心的翻版，也不是博科公司的再现。</p>
        <p>今后我们要突破原有的思维定式，开拓视野，学习最新前沿技术，培养敏捷思维，更有活力地投身到工作中去...</p>
      </div>
    </el-card>

    <!-- 2. 目标 -->
    <el-card id="vision" class="section-card">
      <template #header>
        <div class="card-header">
          <h2>目标</h2>
          <div class="quick-links">
            <el-tag size="small" @click="scrollToSection('bg_introduction')">背景</el-tag>
            <el-tag size="small" @click="scrollToSection('vision')">目标</el-tag>
            <el-tag size="small" @click="scrollToSection('architecture')">架构</el-tag>
            <el-tag size="small" @click="scrollToSection('deploy')">部署</el-tag>
            <el-tag size="small" @click="scrollToSection('config_tab')">数据库表</el-tag>
            <el-tag size="small" @click="scrollToSection('fel_expression')">EL 表达式</el-tag>
          </div>
        </div>
      </template>
      
      <div class="section-content">
        <div class="vision-tags">
          <el-tag size="large" effect="dark" class="vision-tag">十万级作业调度</el-tag>
          <el-tag size="large" effect="dark" class="vision-tag">稳定</el-tag>
          <el-tag size="large" effect="dark" class="vision-tag">功能完善</el-tag>
          <el-tag size="large" effect="dark" class="vision-tag">良好的用户体验</el-tag>
          <el-tag size="large" effect="dark" class="vision-tag">调度云服务</el-tag>
          <el-tag size="large" effect="dark" class="vision-tag">智能调度</el-tag>
        </div>
      </div>
    </el-card>

    <!-- 3. 架构 -->
    <el-card id="architecture" class="section-card">
      <template #header>
        <div class="card-header">
          <h2>架构</h2>
          <div class="quick-links">
            <el-tag size="small" @click="scrollToSection('bg_introduction')">背景</el-tag>
            <el-tag size="small" @click="scrollToSection('vision')">目标</el-tag>
            <el-tag size="small" @click="scrollToSection('architecture')">架构</el-tag>
            <el-tag size="small" @click="scrollToSection('deploy')">部署</el-tag>
            <el-tag size="small" @click="scrollToSection('config_tab')">数据库表</el-tag>
            <el-tag size="small" @click="scrollToSection('fel_expression')">EL 表达式</el-tag>
          </div>
        </div>
      </template>
      
      <div class="section-content">
        <p class="intro-text">
          调度工具的开发采用 Zookeeper + RDB + OSGi 等技术组件或规范，以插件开发应用模式，
          在任务负载均衡，作业类型适配，基础服务等方面提供热部署及动态配置切换。
        </p>
        
        <div class="architecture-diagram">
          <el-row :gutter="20">
            <el-col :span="10">
              <h3>灵活简单分布式作业调度</h3>
              <p>渐进式基础开发框架，为作业的调度或执行提供更多的服务支持。</p>
              <el-descriptions :column="1" border>
                <el-descriptions-item label="Scheduler">
                  集群管理，任务驱动，负载均衡，信息采集，计划任务
                </el-descriptions-item>
                <el-descriptions-item label="Slaver">
                  任务执行，插件注册，计划任务
                </el-descriptions-item>
                <el-descriptions-item label="Registry">
                  插件注册中心，分布式锁，节点及插件注册，Master 选举
                </el-descriptions-item>
              </el-descriptions>
            </el-col>
            <el-col :span="14">
              <img src="/img/demo/wws/wws-architecture.png" alt="WWS 架构图" class="diagram-img" />
            </el-col>
          </el-row>
        </div>

        <h3>方案一：简单部署（单机）</h3>
        <el-divider />
        <div class="deploy-section">
          <el-row :gutter="20">
            <el-col :span="10">
              <h4>单机模式部署</h4>
              <p>无高可用要求，单一产品作业量不多的情况，建议 1 台数据库服务器，1 台调度服务器，1 台 Zookeeper 服务器。</p>
              <el-descriptions :column="1" border size="small">
                <el-descriptions-item label="WWSMaster">
                  启动两个进程，批量进程和 WEB 控制台进程
                </el-descriptions-item>
                <el-descriptions-item label="Zookeeper">
                  单机部署，也可集群部署，3-n 个节点
                </el-descriptions-item>
                <el-descriptions-item label="DBServer">
                  数据库服务器，支持 MySQL 和 Oracle
                </el-descriptions-item>
              </el-descriptions>
            </el-col>
            <el-col :span="14">
              <img src="/img/demo/wws/wws-signal-top.png" alt="单机部署图" class="diagram-img" />
            </el-col>
          </el-row>
        </div>

        <h3>方案二：高可用部署（集群）</h3>
        <el-divider />
        <div class="deploy-section">
          <el-row :gutter="20">
            <el-col :span="10">
              <h4>集群模式部署</h4>
              <p>要求高可用，多产品作业量较多的情况，建议 2 台数据库服务器，3 台（或更多）调度服务器，3 台 Zookeeper 服务器。</p>
              <el-descriptions :column="1" border size="small">
                <el-descriptions-item label="WWSMaster">
                  启动两个进程，批量进程和 WEB 控制台进程
                </el-descriptions-item>
                <el-descriptions-item label="WWSSlave">
                  执行某一个产品或模块具体作业的服务器节点
                </el-descriptions-item>
                <el-descriptions-item label="Zookeeper">
                  最少 3 个节点，或 3-n 个节点（n 为大于 3 的奇数）
                </el-descriptions-item>
                <el-descriptions-item label="DBServer">
                  数据库服务器，支持 MySQL 和 Oracle
                </el-descriptions-item>
              </el-descriptions>
            </el-col>
            <el-col :span="14">
              <img src="/img/demo/wws/wws-cluster-top.png" alt="集群部署图" class="diagram-img" />
            </el-col>
          </el-row>
        </div>
      </div>
    </el-card>

    <!-- 4. 部署 -->
    <el-card id="deploy" class="section-card">
      <template #header>
        <div class="card-header">
          <h2>部署</h2>
          <div class="quick-links">
            <el-tag size="small" @click="scrollToSection('bg_introduction')">背景</el-tag>
            <el-tag size="small" @click="scrollToSection('vision')">目标</el-tag>
            <el-tag size="small" @click="scrollToSection('architecture')">架构</el-tag>
            <el-tag size="small" @click="scrollToSection('deploy')">部署</el-tag>
            <el-tag size="small" @click="scrollToSection('config_tab')">数据库表</el-tag>
            <el-tag size="small" @click="scrollToSection('fel_expression')">EL 表达式</el-tag>
          </div>
        </div>
      </template>
      
      <div class="section-content">
        <p>本节为 WWS 调度 Master、Slave、WebConsole 三个应用程序的安装说明。</p>
        
        <h3>1. 端口使用说明</h3>
        <el-divider />
        <p>
          WWS 运行时，需要开放以下端口：
          <el-tag type="danger" effect="dark" class="port-tag">1099</el-tag>
          <el-tag type="danger" effect="dark" class="port-tag">44444</el-tag>
          <el-tag type="danger" effect="dark" class="port-tag">8101</el-tag>
          <el-tag type="danger" effect="dark" class="port-tag">8585</el-tag>
        </p>
        
        <h3>2. 环境要求</h3>
        <el-divider />
        <ul>
          <li>Java8 或更高版本</li>
          <li>ZOOKEEPER 3.4.12 或更高版本</li>
          <li>MySQL 或 ORACLE</li>
        </ul>
        
        <h3>3. Zookeeper 配置调优说明</h3>
        <el-divider />
        <p><strong>参数调优</strong></p>
        <p>鉴于网络抖动、zookeeper 负载过高、wws 进程虚拟机负载过高等情况，建议修改 zookeeper 的配置文件：</p>
        <pre class="code-block">
minSessionTimeout=10000
maxSessionTimeout=60000
autopurge.purgeInterval=2
autopurge.snapRetainCount=10
snapCount=1000000
maxClientCnxns=2000
        </pre>
        
        <h3>4. WebConsole 服务安装</h3>
        <el-divider />
        <p>详细安装步骤请参考实际部署文档...</p>
      </div>
    </el-card>

    <!-- 5. 数据库表 -->
    <el-card id="config_tab" class="section-card">
      <template #header>
        <div class="card-header">
          <h2>作业配置表</h2>
          <div class="quick-links">
            <el-tag size="small" @click="scrollToSection('bg_introduction')">背景</el-tag>
            <el-tag size="small" @click="scrollToSection('vision')">目标</el-tag>
            <el-tag size="small" @click="scrollToSection('architecture')">架构</el-tag>
            <el-tag size="small" @click="scrollToSection('deploy')">部署</el-tag>
            <el-tag size="small" @click="scrollToSection('config_tab')">数据库表</el-tag>
            <el-tag size="small" @click="scrollToSection('fel_expression')">EL 表达式</el-tag>
          </div>
        </div>
      </template>
      
      <div class="section-content">
        <h3>主要配置表</h3>
        <el-table :data="mainTables" border stripe class="table-list">
          <el-table-column prop="tableName" label="表名" width="200" />
          <el-table-column prop="tableCnName" label="表中文名" width="200" />
          <el-table-column prop="description" label="描述" />
        </el-table>
        
        <h3>详细说明</h3>
        <p>详细的表结构说明请参考设计文档...</p>
      </div>
    </el-card>

    <!-- 6. FEL 表达式 -->
    <el-card id="fel_expression" class="section-card">
      <template #header>
        <div class="card-header">
          <h2>FEL 表达式使用说明</h2>
          <div class="quick-links">
            <el-tag size="small" @click="scrollToSection('bg_introduction')">背景</el-tag>
            <el-tag size="small" @click="scrollToSection('vision')">目标</el-tag>
            <el-tag size="small" @click="scrollToSection('architecture')">架构</el-tag>
            <el-tag size="small" @click="scrollToSection('deploy')">部署</el-tag>
            <el-tag size="small" @click="scrollToSection('config_tab')">数据库表</el-tag>
            <el-tag size="small" @click="scrollToSection('fel_expression')">EL 表达式</el-tag>
          </div>
        </div>
      </template>
      
      <div class="section-content">
        <h3>时间处理函数</h3>
        <el-table :data="dateFunctions" border stripe class="function-table">
          <el-table-column prop="funcName" label="函数名" width="120" />
          <el-table-column prop="func" label="函数" width="250" />
          <el-table-column prop="returns" label="返回类型" width="180" />
          <el-table-column prop="description" label="说明" />
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

// 数据表信息
interface TableInfo {
  tableName: string
  tableCnName: string
  description: string
}

const mainTables = ref<TableInfo[]>([
  { tableName: 'ww_job', tableCnName: 'job 模板配置表', description: '配置 job 模板' },
  { tableName: 'ww_task', tableCnName: 'task 模板定义表', description: '配置 job 模板下的某个 task 模板' },
  { tableName: 'ww_task_lean', tableCnName: 'job 内部 task 依赖关系表', description: '配置同一个 job 模板下 job 模板之间的依赖关系' },
  { tableName: 'ww_parameter', tableCnName: '资源对象的属性列表', description: '配置 job 模板，task 模板，插件的独立参数和公共参数等' },
  { tableName: 'ww_dimension', tableCnName: '维度表', description: '配置维度信息' },
  { tableName: 'ww_dimension_entity', tableCnName: '维度实体表', description: '配置某个维度包含的维度实体信息' },
  { tableName: 'ww_job_dimension', tableCnName: 'job 模板维度方案表', description: '配置 job 模板的维度方案' },
  { tableName: 'ww_task_dimension', tableCnName: 'task 维度设置表', description: '配置 task 模板的维度方案' },
  { tableName: 'ww_job_instance', tableCnName: 'job 模板实例表', description: '启动 job 模板之后，存储生成的 job 模板实例' },
  { tableName: 'ww_task_instance', tableCnName: 'task 模板实例表', description: '启动 job 模板之后，存储生成的 task 模板实例' }
])

// 函数信息
interface FunctionInfo {
  funcName: string
  func: string
  returns: string
  description: string
}

const dateFunctions = ref<FunctionInfo[]>([
  { funcName: 'date', func: "date()", returns: 'java.util.Date', description: '获取当前时间' },
  { funcName: 'date', func: "date(String pattern)", returns: 'java.lang.String', description: '获取当前时间字符串' },
  { funcName: 'date', func: "date(Integer increment, String field)", returns: 'java.util.Date', description: '获取计算后的日期' },
  { funcName: 'to_char', func: "to_char(Date date, String pattern)", returns: 'java.lang.String', description: '日期转字符串' },
  { funcName: 'to_date', func: "to_date(String date, String pattern)", returns: 'java.util.Date', description: '字符串转日期' },
  { funcName: 'next_day', func: "next_day()", returns: 'java.lang.String', description: '获取明日日期' },
  { funcName: 'next_month', func: "next_month()", returns: 'java.lang.String', description: '下月的今天' }
])

// 滚动到指定区域
const scrollToSection = (sectionId: string) => {
  const element = document.getElementById(sectionId)
  if (element) {
    element.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}
</script>

<style scoped>
.product-manual {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.breadcrumb {
  margin-bottom: 20px;
}

.page-header {
  margin: 20px 0;
}

.page-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 10px;
}

.nav-intro {
  margin: 20px 0;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
  line-height: 2;
}

.nav-tag {
  cursor: pointer;
  transition: all 0.3s;
}

.nav-tag:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

.section-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-size: 1.375rem;
  font-weight: 700;
}

.quick-links {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.quick-links .el-tag {
  cursor: pointer;
  transition: all 0.3s;
}

.quick-links .el-tag:hover {
  transform: scale(1.05);
}

.section-content {
  padding: 10px 0;
}

.section-content h3 {
  font-size: 1.2rem;
  font-weight: 600;
  margin: 20px 0 10px;
  color: #303133;
}

.section-content h4 {
  font-size: 1.1rem;
  font-weight: 600;
  margin: 15px 0 10px;
  color: #409eff;
}

.section-content p {
  line-height: 1.8;
  color: #606266;
  margin: 10px 0;
}

.section-content ul {
  padding-left: 20px;
  line-height: 2;
}

.vision-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  padding: 20px 0;
}

.vision-tag {
  padding: 10px 20px;
  font-size: 1rem;
  font-weight: 600;
}

.architecture-diagram,
.deploy-section {
  margin: 30px 0;
  padding: 20px;
  background-color: #fafafa;
  border-radius: 4px;
}

.diagram-img {
  width: 100%;
  max-width: 100%;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 10px;
  background-color: #fff;
}

.port-tag {
  margin: 0 5px;
  font-weight: 600;
}

.code-block {
  background-color: #282c34;
  color: #abb2bf;
  padding: 15px;
  border-radius: 4px;
  font-family: 'Courier New', Courier, monospace;
  font-size: 13px;
  line-height: 1.5;
  overflow-x: auto;
  margin: 15px 0;
}

.table-list,
.function-table {
  margin: 20px 0;
}

:deep(.el-card__header) {
  padding: 15px 20px;
  background-color: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
}

:deep(.el-descriptions__label) {
  font-weight: 500;
  width: 150px;
}

:deep(.el-table th) {
  background-color: #f5f7fa;
  color: #606266;
  font-weight: 600;
}
</style>

```