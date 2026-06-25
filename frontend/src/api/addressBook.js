import request from '@/utils/request'

export const getAddressBookList = () => {
  return request({
    url: '/addressBook/list',
    method: 'get'
  })
}

export const createAddressBook = (data) => {
  return request({
    url: '/addressBook',
    method: 'post',
    data
  })
}

export const getDefaultAddressBook = () => {
  return request({
    url: '/addressBook/default',
    method: 'get'
  })
}

export const setDefaultAddressBook = (id) => {
  return request({
    url: '/addressBook/default',
    method: 'put',
    data: { id }
  })
}

export const updateAddressBook = (data) => {
  return request({
    url: '/addressBook',
    method: 'put',
    data
  })
}

export const deleteAddressBook = (id) => {
  return request({
    url: '/addressBook',
    method: 'delete',
    params: { id }
  })
}
