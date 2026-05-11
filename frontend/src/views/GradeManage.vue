<template>
  <div class="grade-manage-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span>成绩录入管理</span>
          </div>
          <div class="header-right">
             <el-switch
                v-model="isBatchMode"
                active-text="批量录入模式"
                inactive-text="普通模式"
                style="margin-right: 20px"
             />
             <el-button v-if="isBatchMode" type="success" @click="handleBatchSave" :loading="batchSaving">保存全部变更</el-button>
             <div v-else style="display: inline-block">
                <el-select v-model="filterTerm" placeholder="筛选学期" clearable style="width: 120px; margin-right: 10px">
                    <el-option v-for="term in termOptions" :key="term" :label="term" :value="term" />
                </el-select>
                <el-select v-model="filterCourse" placeholder="筛选课程" clearable style="width: 120px; margin-right: 10px">
                    <el-option v-for="course in courses" :key="course.id" :label="course.name" :value="course.id" />
                </el-select>
                <el-input v-model="filterStudent" placeholder="搜索学生姓名" style="width: 120px; margin-right: 10px" clearable />
                <el-button type="primary" @click="handleAdd">录入成绩</el-button>
             </div>
          </div>
        </div>
      </template>
      
      <el-table :data="filteredTableData" style="width: 100%" v-loading="loading" border stripe :row-class-name="tableRowClassName">
        <el-table-column prop="id" label="ID" width="60" align="center" />
        <el-table-column prop="term" label="学期" align="center" sortable width="120" />
        <el-table-column prop="courseId" label="课程" align="center" sortable>
             <template #default="scope">
                {{ getCourseName(scope.row.courseId) }}
             </template>
        </el-table-column>
        <el-table-column prop="studentId" label="学生" align="center" sortable>
             <template #default="scope">
                {{ getStudentName(scope.row.studentId) }}
             </template>
        </el-table-column>
        <el-table-column label="班级" align="center" width="120">
             <template #default="scope">
                {{ getStudentClass(scope.row.studentId) }}
             </template>
        </el-table-column>
        <el-table-column prop="score" label="成绩" align="center" sortable width="180">
             <template #default="scope">
                <div v-if="isBatchMode">
                    <el-input-number 
                        v-model="scope.row.score" 
                        :min="0" :max="100" 
                        controls-position="right"
                        size="small"
                        :class="{'is-error': !isValidScore(scope.row.score)}"
                        @change="handleScoreChange(scope.row)"
                    />
                    <div v-if="!isValidScore(scope.row.score)" class="error-tip">0-100</div>
                </div>
                <span v-else :class="getScoreClass(scope.row.score)">{{ scope.row.score }}</span>
             </template>
        </el-table-column>
        <el-table-column prop="makeupScore" label="补考成绩" align="center" width="180">
             <template #default="scope">
                <div v-if="isBatchMode">
                     <el-input-number 
                        v-model="scope.row.makeupScore" 
                        :min="0" :max="100" 
                        controls-position="right"
                        size="small"
                        placeholder="-"
                        @change="handleScoreChange(scope.row)"
                    />
                </div>
                <div v-else>
                    <span v-if="scope.row.makeupScore !== null" :class="scope.row.makeupScore < 60 ? 'score-fail' : ''">{{ scope.row.makeupScore }}</span>
                    <span v-else>-</span>
                </div>
             </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">修改</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)" v-if="userStore.role === 'ADMIN'">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle">
      <el-form :model="form" label-width="80px">
        <el-form-item label="学期">
           <el-input v-model="form.term" placeholder="例如: 2023-Fall" />
        </el-form-item>
        <el-form-item label="课程">
          <el-select v-model="form.courseId" placeholder="请选择课程">
            <el-option
              v-for="item in courses"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="学生">
          <el-select v-model="form.studentId" placeholder="请选择学生" filterable>
            <el-option
              v-for="item in students"
              :key="item.id"
              :label="item.name + ' (' + item.username + ')'"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="成绩">
          <el-input-number v-model="form.score" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="补考成绩">
          <el-input-number v-model="form.makeupScore" :min="0" :max="100" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed } from 'vue'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const userStore = useUserStore()
const loading = ref(false)
const tableData = ref([])
const courses = ref([])
const allCourses = ref([])
const students = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')

const filterTerm = ref('')
const filterCourse = ref('')
const filterStudent = ref('')

const isBatchMode = ref(false)
const batchSaving = ref(false)
const modifiedRows = ref(new Set())

const termOptions = computed(() => {
    return [...new Set(tableData.value.map(item => item.term))].sort()
})

const filteredTableData = computed(() => {
    return tableData.value.filter(item => {
        // Use loose comparison for ID to handle string/number mismatch
        const student = students.value.find(s => s.id == item.studentId)
        
        // Strictly filter out students not in the allowed list (e.g. not in class)
        if (!student) return false

        const matchTerm = !filterTerm.value || item.term === filterTerm.value
        const matchCourse = !filterCourse.value || item.courseId == filterCourse.value
        const sName = student.name
        const matchStudent = !filterStudent.value || sName.includes(filterStudent.value)
        return matchTerm && matchCourse && matchStudent
    })
})

const isValidScore = (score) => {
    return score !== null && score >= 0 && score <= 100
}

const handleScoreChange = (row) => {
    modifiedRows.value.add(row.id)
}

const handleBatchSave = async () => {
    if (modifiedRows.value.size === 0) {
        ElMessage.info('没有需要保存的更改')
        return
    }
    
    // Validate all modified rows
    const rowsToSave = tableData.value.filter(row => modifiedRows.value.has(row.id))
    const invalidRows = rowsToSave.filter(row => !isValidScore(row.score) || (row.makeupScore !== null && !isValidScore(row.makeupScore)))
    
    if (invalidRows.length > 0) {
        ElMessage.error(`存在 ${invalidRows.length} 条无效数据，请修正后再保存`)
        return
    }
    
    batchSaving.value = true
    try {
        const promises = rowsToSave.map(row => request.put(`/grades/${row.id}`, row))
        await Promise.all(promises)
        ElMessage.success(`成功保存 ${rowsToSave.length} 条记录`)
        modifiedRows.value.clear()
        isBatchMode.value = false
        fetchData()
    } catch (error) {
        ElMessage.error('部分保存失败，请刷新重试')
    } finally {
        batchSaving.value = false
    }
}

const form = reactive({
  id: null,
  studentId: null,
  courseId: null,
  score: 0,
  makeupScore: null,
  term: '2023-Fall'
})

const getCourseName = (id) => {
    const course = allCourses.value.find(c => c.id == id)
    return course ? course.name : id
}

const getStudentName = (id) => {
    const student = students.value.find(s => s.id == id)
    return student ? student.name : id
}

const getStudentClass = (id) => {
    const student = students.value.find(s => s.id == id)
    return student ? (student.className || '未知班级') : '-'
}

const fetchData = async () => {
  loading.value = true
  try {
    let gradesUrl = '/grades'
    let coursesUrl = '/courses'
    if (userStore.role === 'TEACHER') {
        gradesUrl = `/grades/teacher/${userStore.user.id}`
    } else if (userStore.role === 'HEAD_TEACHER' && userStore.user.className) {
        gradesUrl = `/grades/class/${userStore.user.className}`
        coursesUrl = `/courses/class/${userStore.user.className}`
    }
    
    const [gradesRes, coursesRes, usersRes] = await Promise.all([
        request.get(gradesUrl),
        request.get(coursesUrl),
        request.get('/users')
    ])
    
    tableData.value = gradesRes
    allCourses.value = coursesRes
    courses.value = coursesRes
    students.value = usersRes.filter(u => u.role === 'STUDENT')
    
    // If teacher, filter courses they teach
    if (userStore.role === 'TEACHER') {
        courses.value = coursesRes.filter(c => c.teacherId === userStore.user.id)
    }
    
    // If head teacher, filter students to their class
    if (userStore.role === 'HEAD_TEACHER' && userStore.user.className) {
        students.value = students.value.filter(s => s.className === userStore.user.className)
    }
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogTitle.value = '录入成绩'
  Object.assign(form, {
    id: null,
    studentId: null,
    courseId: null,
    score: 0,
    makeupScore: null,
    term: '2023-Fall'
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '修改成绩'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该成绩记录吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await request.delete(`/grades/${row.id}`)
    ElMessage.success('删除成功')
    fetchData()
  })
}

const handleSubmit = async () => {
  try {
    if (form.id) {
      await request.put(`/grades/${form.id}`, form)
      ElMessage.success('修改成功')
    } else {
      await request.post('/grades', form)
      ElMessage.success('录入成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (error) {
    // Error handled
  }
}

const getScoreClass = (score) => {
    if (score < 60) return 'score-fail'
    if (score >= 90) return 'score-excellent'
    return ''
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.grade-manage-container {
    min-height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
    display: flex;
    align-items: center;
}

.header-left span {
    font-size: 18px;
    font-weight: 600;
    color: var(--text-primary);
    font-family: 'Montserrat', sans-serif;
    margin-right: 15px;
}

.header-right {
    display: flex;
    align-items: center;
}

.score-fail {
    color: #EF4444;
    font-weight: 600;
    font-family: 'Roboto', sans-serif;
}

.score-excellent {
    color: #10B981;
    font-weight: 600;
    font-family: 'Roboto', sans-serif;
}

.error-tip {
    color: #EF4444;
    font-size: 12px;
    line-height: 1;
    margin-top: 4px;
    font-family: 'Roboto', sans-serif;
}

:deep(.is-error .el-input__wrapper) {
    box-shadow: 0 0 0 1px #EF4444 inset;
}
</style>
