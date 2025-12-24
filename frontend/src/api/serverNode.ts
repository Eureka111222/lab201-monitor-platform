import { request } from '@/utils/request'
import type { PageResult, PaginationQuery, ServerNode } from './types'

export const createServerNode = (data: ServerNode) => {
  return request<boolean>({
    url: '/server-node',
    method: 'post',
    data,
  })
}

export const updateServerNode = (data: ServerNode) => {
  return request<boolean>({
    url: '/server-node',
    method: 'put',
    data,
  })
}

export const deleteServerNode = (id: number) => {
  return request<boolean>({
    url: `/server-node/${id}`,
    method: 'delete',
  })
}

export const fetchServerNodePage = (params?: PaginationQuery) => {
  return request<PageResult<ServerNode>>({
    url: '/server-node/page',
    method: 'get',
    params,
  })
}

export const fetchAllServerNodes = () => {
  return request<ServerNode[]>({
    url: '/server-node/list',
    method: 'get',
  })
}

