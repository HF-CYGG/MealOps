import request from '@/utils/request'

export const uploadFile = (file) => {
  const formData = new FormData()
  formData.append('file', file)

  return request({
    url: '/common/upload',
    method: 'post',
    data: formData
  })
}
