import { del, get, post, put } from '@/utils/request'

export interface ContentPage<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
  totalPages: number
  counts?: Record<string, number>
}

export interface TypeOption {
  id: number
  name: string
}

export interface TopicTypeRow {
  id: number
  code: string
  name: string
  icon?: string
  intro?: string
  visible: boolean
  sort: number
  topicCount: number
}

export interface TopicTypeForm {
  id?: number
  name: string
  sort: number
  visible: boolean
  icon: string
  intro: string
}

export interface TopicRow {
  id: string
  typeId: number
  typeName: string
  title: string
  publishTime: string
  productCount: number
  clickCount: number
  collectCount: number
  commentCount: number
  sort: number
  status: number
  statusText: string
}

export interface TopicForm {
  topicCode?: string
  typeId: number | ''
  title: string
  intro: string
  content: string
  coverImage: string
  specifyProducts: boolean
  productIds: string[]
  memberLevels: string[]
  regions: string[][]
  tags: Record<string, string[]>
  estimatedUsers: number
  sort: number
  status?: number
}

export interface TopicDetail extends TopicForm {
  id: string
  images: string[]
  collectCount: number
  readCount: number
  shareCount: number
  products: { id: string; name: string; price: number; thumb?: string }[]
  comments: TopicCommentRow[]
}

export interface TopicCommentRow {
  id: string
  content: string
  replyContent: string
  pics: string[]
  status: number
  statusText: string
}

export interface HelpTypeRow {
  id: number
  code: string
  name: string
  icon?: string
  visible: boolean
  sort: number
  articleCount: number
}

export interface HelpTypeForm {
  id?: number
  name: string
  sort: number
  visible: boolean
  icon: string
}

export interface HelpArticleRow {
  id: string
  typeId: number
  categoryName: string
  title: string
  publishTime: string
  clickCount: number
  sort: number
  status: number
  statusText: string
}

export interface HelpArticleForm {
  articleCode?: string
  typeId: number | ''
  title: string
  intro: string
  content: string
  coverImages: string[]
  online: boolean
  sort?: number
}

export function createTopicTypeForm(): TopicTypeForm {
  return { name: '', sort: 0, visible: true, icon: '', intro: '' }
}

export function createTopicForm(): TopicForm {
  return {
    typeId: '',
    title: '',
    intro: '',
    content: '',
    coverImage: '',
    specifyProducts: true,
    productIds: [],
    memberLevels: ['all'],
    tags: { newUser: ['all'], firstBuy: ['all'], repurchase: ['all'], active: ['all'] },
    estimatedUsers: 0,
    sort: 0,
    status: 1,
  }
}

export function createHelpTypeForm(): HelpTypeForm {
  return { name: '', sort: 0, visible: true, icon: '' }
}

export function createHelpForm(): HelpArticleForm {
  return { typeId: '', title: '', intro: '', content: '', coverImages: [], online: true, sort: 0 }
}

// ---------- 专题类型 ----------
export function fetchTopicTypeList(params: {
  keyword?: string
  tab?: string
  page?: number
  pageSize?: number
}) {
  return get<ContentPage<TopicTypeRow>>('/api/content/topic/type/list', { params })
}

export function fetchTopicTypeOptions() {
  return get<TypeOption[]>('/api/content/topic/type/options')
}

export function fetchTopicTypeDetail(id: number) {
  return get<TopicTypeForm & { code?: string }>(`/api/content/topic/type/${id}`)
}

export function saveTopicType(data: TopicTypeForm & { id?: number }) {
  return post<number>('/api/content/topic/type/save', data)
}

export function toggleTopicTypeVisible(id: number, visible: boolean) {
  return put<void>(`/api/content/topic/type/${id}/visible`, { visible })
}

export function deleteTopicType(id: number) {
  return del<void>(`/api/content/topic/type/${id}`)
}

export function batchDeleteTopicTypes(ids: number[]) {
  return post<{ count: number }>('/api/content/topic/type/batch/delete', { ids })
}

// ---------- 专题 ----------
export function fetchTopicList(params: {
  title?: string
  tab?: string
  startDate?: string
  endDate?: string
  page?: number
  pageSize?: number
}) {
  return get<ContentPage<TopicRow>>('/api/content/topic/list', { params })
}

export function fetchTopicDetail(topicCode: string) {
  return get<TopicDetail>(`/api/content/topic/detail/${topicCode}`)
}

export function fetchTopicForEdit(topicCode: string) {
  return get<TopicDetail>(`/api/content/topic/${topicCode}`)
}

export function saveTopic(data: TopicForm & { topicCode?: string }) {
  return post<string>('/api/content/topic/save', data)
}

export function updateTopicStatus(topicCode: string, status: number) {
  return put<void>(`/api/content/topic/${topicCode}/status`, { status })
}

export function deleteTopic(topicCode: string) {
  return del<void>(`/api/content/topic/${topicCode}`)
}

export function replyTopicComment(commentCode: string, replyContent: string) {
  return post<void>('/api/content/topic/comment/reply', { commentCode, replyContent })
}

export function reviewTopicComment(commentCode: string, action: 'feature' | 'hide' | 'show') {
  return post<void>('/api/content/topic/comment/review', { commentCode, action })
}

export function deleteTopicComment(commentCode: string) {
  return del<void>(`/api/content/topic/comment/${commentCode}`)
}

export function batchDeleteTopicComments(ids: string[]) {
  return post<{ count: number }>('/api/content/topic/comment/batch/delete', { ids })
}

// ---------- 帮助类型 ----------
export function fetchHelpTypeList(params: {
  keyword?: string
  tab?: string
  page?: number
  pageSize?: number
}) {
  return get<ContentPage<HelpTypeRow>>('/api/content/help/type/list', { params })
}

export function fetchHelpTypeOptions() {
  return get<TypeOption[]>('/api/content/help/type/options')
}

export function fetchHelpTypeDetail(id: number) {
  return get<HelpTypeForm & { code?: string }>(`/api/content/help/type/${id}`)
}

export function saveHelpType(data: HelpTypeForm & { id?: number }) {
  return post<number>('/api/content/help/type/save', data)
}

export function toggleHelpTypeVisible(id: number, visible: boolean) {
  return put<void>(`/api/content/help/type/${id}/visible`, { visible })
}

export function deleteHelpType(id: number) {
  return del<void>(`/api/content/help/type/${id}`)
}

export function batchDeleteHelpTypes(ids: number[]) {
  return post<{ count: number }>('/api/content/help/type/batch/delete', { ids })
}

// ---------- 帮助文章 ----------
export function fetchHelpList(params: {
  title?: string
  tab?: string
  startDate?: string
  endDate?: string
  page?: number
  pageSize?: number
}) {
  return get<ContentPage<HelpArticleRow>>('/api/content/help/list', { params })
}

export function fetchHelpDetail(articleCode: string) {
  return get<HelpArticleForm>(`/api/content/help/${articleCode}`)
}

export function saveHelpArticle(data: HelpArticleForm) {
  return post<string>('/api/content/help/save', data)
}

export function updateHelpStatus(articleCode: string, status: number) {
  return put<void>(`/api/content/help/${articleCode}/status`, { status })
}

export function deleteHelpArticle(articleCode: string) {
  return del<void>(`/api/content/help/${articleCode}`)
}
