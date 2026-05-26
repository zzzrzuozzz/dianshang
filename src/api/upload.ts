import { post } from '@/utils/request'

export interface UploadResult {
  url: string
}

/** POST /api/upload/image */
export function uploadImage(file: File, biz = 'ops') {
  const form = new FormData()
  form.append('file', file)
  return post<UploadResult>('/api/upload/image', form, {
    params: { biz },
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}
