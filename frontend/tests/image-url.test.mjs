import assert from 'node:assert/strict'
import { FALLBACK_DISH_IMAGE, resolveImageUrl } from '../src/utils/image.js'

assert.equal(resolveImageUrl(''), FALLBACK_DISH_IMAGE)
assert.equal(resolveImageUrl(null), FALLBACK_DISH_IMAGE)
assert.equal(resolveImageUrl('https://example.com/dish.jpg'), 'https://example.com/dish.jpg')
assert.equal(resolveImageUrl('data:image/png;base64,abc'), 'data:image/png;base64,abc')
assert.equal(resolveImageUrl('/common/download?name=abc.jpg'), '/api/common/download?name=abc.jpg')
assert.equal(resolveImageUrl('/images/dish-gongbao.jpg'), '/api/images/dish-gongbao.jpg')
assert.equal(resolveImageUrl('images/dish-yuxiang.jpg'), '/api/images/dish-yuxiang.jpg')
assert.equal(resolveImageUrl('common/download?name=abc.jpg'), '/api/common/download?name=abc.jpg')
