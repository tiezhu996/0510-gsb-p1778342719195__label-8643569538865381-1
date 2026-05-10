<template>
  <div class="dashboard-container">
    <el-row :gutter="24">
      <el-col :span="8" :xs="24">
        <el-card class="welcome-card" shadow="hover">
          <div class="welcome-header">
            <el-avatar :size="64" class="welcome-avatar" :src="avatarUrl">{{ userStore.user?.name?.charAt(0) }}</el-avatar>
            <div class="welcome-text">
              <h3>早安，{{ userStore.user?.name }}，祝你今天过得开心！</h3>
              <p>{{ roleName }} | {{ userStore.user?.className || '暂无班级' }}</p>
            </div>
          </div>
          <el-divider />
          <div class="user-stats">
            <div class="stat-item">
              <span class="label">上次登录</span>
              <span class="value">2023-10-27</span>
            </div>
            <div class="stat-item">
              <span class="label">待办事项</span>
              <span class="value">0</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="16" :xs="24">
        <el-card class="announcement-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="header-title"><el-icon><Bell /></el-icon> 系统公告</span>
            
            </div>
          </template>
          <div class="announcement-list">
            <div class="announcement-item">
              <el-tag size="small" type="danger" effect="light">重要</el-tag>
              <span class="title">关于本学期期末考试成绩录入的通知</span>
              <span class="date">2023-12-01</span>
            </div>
            <div class="announcement-item">
              <el-tag size="small" type="primary" effect="light">通知</el-tag>
              <span class="title">系统维护升级公告 (v1.2.0)</span>
              <span class="date">2023-11-20</span>
            </div>
            <div class="announcement-item">
              <el-tag size="small" type="success" effect="light">消息</el-tag>
              <span class="title">欢迎新同学加入成绩管理系统</span>
              <span class="date">2023-09-01</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="24" style="margin-top: 24px;">
      <el-col :span="24">
         <el-card shadow="hover" class="chart-card">
            <template #header>
              <div class="card-header">
                <span class="header-title"><el-icon><TrendCharts /></el-icon> 成绩概览</span>
                <el-radio-group v-model="chartPeriod" size="small">
                  <el-radio-button label="week">本周</el-radio-button>
                  <el-radio-button label="month">本月</el-radio-button>
                  <el-radio-button label="term">本学期</el-radio-button>
                </el-radio-group>
              </div>
            </template>
            <div ref="chartRef" style="height: 350px;"></div>
         </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useUserStore } from '@/stores/user'
import * as echarts from 'echarts'
import { Bell, TrendCharts } from '@element-plus/icons-vue'

const userStore = useUserStore()
const chartRef = ref(null)
const chartPeriod = ref('term')
const avatarUrl = ref('') // Placeholder for future real avatar

const roleName = computed(() => {
  const map = {
    'ADMIN': '管理员',
    'TEACHER': '教师',
    'HEAD_TEACHER': '班主任',
    'STUDENT': '学生'
  }
  return map[userStore.user?.role] || userStore.user?.role
})

onMounted(async () => {
  initChart()
})

const initChart = () => {
  const myChart = echarts.init(chartRef.value)
  
  let option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['高等数学', '大学英语', '计算机基础', '数据结构', '操作系统', '计算机网络', '软件工程'],
      axisLine: { lineStyle: { color: '#909399' } },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: { lineStyle: { type: 'dashed', color: '#E4E7ED' } }
    },
    series: [
      {
        name: '平均分',
        type: 'bar',
        barWidth: '40%',
        data: [85, 90, 78, 82, 88, 76, 92],
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#4F46E5' },
            { offset: 1, color: '#818CF8' }
          ]),
          borderRadius: [4, 4, 0, 0]
        },
        showBackground: true,
        backgroundStyle: {
          color: 'rgba(180, 180, 180, 0.1)'
        }
      }
    ]
  }

  myChart.setOption(option)
  window.addEventListener('resize', () => {
    myChart.resize()
  })
}
</script>

<style scoped>
.dashboard-container {
  padding: 0;
}

.welcome-card {
  height: 100%;
}

.welcome-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
}

.welcome-avatar {
  background: var(--gradient-primary);
  font-size: 24px;
  color: white;
  font-weight: bold;
}

.welcome-text h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
  color: var(--text-primary);
  font-weight: 600;
}

.welcome-text p {
  margin: 0;
  color: var(--text-secondary);
  font-size: 14px;
}

.user-stats {
  display: flex;
  justify-content: space-around;
  padding: 10px 0;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
}

.stat-item .label {
  font-size: 12px;
  color: var(--text-light);
}

.stat-item .value {
  font-size: 18px;
  font-weight: bold;
  color: var(--text-primary);
  font-family: 'Roboto', sans-serif;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.announcement-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.announcement-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 0;
  border-bottom: 1px solid var(--bg-color);
  transition: all 0.2s;
}

.announcement-item:hover {
  transform: translateX(5px);
}

.announcement-item:last-child {
  border-bottom: none;
}

.announcement-item .title {
  flex: 1;
  color: var(--text-secondary);
  font-size: 14px;
  cursor: pointer;
  transition: color 0.2s;
}

.announcement-item .title:hover {
  color: var(--primary-color);
}

.announcement-item .date {
  font-size: 12px;
  color: var(--text-light);
}
</style>
