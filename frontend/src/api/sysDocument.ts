import { request } from '@/utils/request'
import type { PageResult, PaginationQuery, SysDocument } from './types'

export const uploadDocument = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)

  return request<string>({
    url: '/sys-document/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

export const fetchDocumentPage = (params?: PaginationQuery) => {
  return request<PageResult<SysDocument>>({
    url: '/sys-document/page',
    method: 'get',
    params,
  })
}

export const deleteDocument = (id: number) => {
  return request<boolean>({
    url: `/sys-document/${id}`,
    method: 'delete',
  })
}

