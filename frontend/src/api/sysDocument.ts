import instance from '@/utils/request' // 引入你封装的 axios 实例

// 分页查询
export const fetchDocumentPage = (params: any) => {
  return instance.get('/sys-document/page', { params })
}

// 🔥🔥 关键修改：上传接口
export const uploadDocument = (data: FormData) => {
  return instance.post('/sys-document/upload', data, {
    headers: {
      // 必须显式告诉后端这是文件上传，虽然 axios 传 FormData 时通常会自动设置，
      // 但为了保险，我们可以手动指定，或者让浏览器自动计算 boundary
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 获取下载链接
export const getDownloadUrl = (id: number) => {
  return instance.get(`/sys-document/download/${id}`)
}

// 删除文档
export const deleteDocument = (id: number) => {
  return instance.delete(`/sys-document/${id}`)
}