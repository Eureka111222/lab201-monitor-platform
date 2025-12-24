import { request } from '@/utils/request'
import type { PageResult, PaginationQuery, SysUser } from './types'

export const createUser = (data: SysUser) => {
  return request<boolean>({
    url: '/sys-user',
    method: 'post',
    data,
  })
}

export const updateUser = (data: SysUser) => {
  return request<boolean>({
    url: '/sys-user',
    method: 'put',
    data,
  })
}

export const getUserById = (id: number) => {
  return request<SysUser>({
    url: `/sys-user/${id}`,
    method: 'get',
  })
}

export const deleteUser = (id: number) => {
  return request<boolean>({
    url: `/sys-user/${id}`,
    method: 'delete',
  })
}

export const fetchUserPage = (params?: PaginationQuery) => {
  return request<PageResult<SysUser>>({
    url: '/sys-user/page',
    method: 'get',
    params,
  })
}

export function login(data: any) {
  return request<any>({
    url: '/sys-user/login',
    method: 'post',
    data
  })
}
