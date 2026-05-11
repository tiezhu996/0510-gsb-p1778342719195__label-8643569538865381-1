import axios from 'axios'
import { ElMessage } from 'element-plus'

const service = axios.create({
  baseURL: '/api',
  timeout: 5000
})

service.interceptors.request.use(
  config => {
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    let msg = '请求失败，请稍后重试'
    const data = error.response?.data
    if (typeof data === 'string') {
      msg = data
    } else if (data && typeof data === 'object') {
      msg = data.message || data.error || msg
    } else if (error.message && /timeout/i.test(error.message)) {
      msg = '请求超时，请检查网络后重试'
    } else if (error.message && /Network Error/i.test(error.message)) {
      msg = '网络错误，请稍后重试'
    }
    if (msg === 'Invalid credentials') {
      msg = '用户名或密码错误'
    }
    ElMessage.error(msg)
    return Promise.reject(error)
  }
)

export default service
