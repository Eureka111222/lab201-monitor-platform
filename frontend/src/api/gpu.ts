import { request } from '@/utils/request'
import type { GpuMetricLog, GpuReportDto, PageResult, PaginationQuery } from './types'

export interface GpuLogQuery extends PaginationQuery {
  serverName?: string
}

export const reportGpuData = (data: GpuReportDto) => {
  return request<string>({
    url: '/gpu/report',
    method: 'post',
    data,
  })
}

export const fetchGpuLogs = (params?: GpuLogQuery) => {
  return request<PageResult<GpuMetricLog>>({
    url: '/gpu/list',
    method: 'get',
    params,
  })
}

