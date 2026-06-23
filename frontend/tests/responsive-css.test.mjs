import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, resolve } from 'node:path'

const __dirname = dirname(fileURLToPath(import.meta.url))
const root = resolve(__dirname, '..')

const read = (path) => readFileSync(resolve(root, path), 'utf8')

const globalCss = read('src/style.css')
const adminLayout = read('src/layout/Index.vue')
const clientLayout = read('src/layout/ClientLayout.vue')
const clientMenu = read('src/views/client/menu/Index.vue')
const dishPage = read('src/views/dish/Index.vue')

assert.match(globalCss, /overflow-x:\s*hidden/, 'global CSS must prevent viewport horizontal overflow')
assert.doesNotMatch(globalCss, /width:\s*100vw/, 'global app container should not force 100vw horizontal overflow')

assert.match(adminLayout, /@media\s*\(max-width:\s*768px\)/, 'admin layout must define a mobile breakpoint')
assert.match(adminLayout, /\.layout-container[\s\S]*?overflow-x:\s*hidden/, 'admin shell must constrain horizontal overflow')
assert.match(adminLayout, /\.sidebar[\s\S]*?width:\s*72px/, 'admin sidebar must collapse on mobile')

assert.match(clientLayout, /@media\s*\(max-width:\s*768px\)/, 'client layout must define a mobile breakpoint')
assert.match(clientLayout, /\.client-header[\s\S]*?flex-wrap:\s*wrap/, 'client header must wrap on narrow screens')
assert.match(clientLayout, /\.client-header[\s\S]*?box-sizing:\s*border-box/, 'client header padding must stay inside the viewport')
assert.match(clientLayout, /\.client-header[\s\S]*?width:\s*calc\(100%\s*-\s*24px\)/, 'mobile client header must account for side margins')
assert.match(clientLayout, /@media\s*\(max-width:\s*480px\)[\s\S]*?display:\s*grid/, 'very small client header must use a stable two-column grid')

assert.match(clientMenu, /@media\s*\(max-width:\s*768px\)/, 'client menu must define a mobile breakpoint')
assert.match(clientMenu, /\.main-content[\s\S]*?flex-direction:\s*column/, 'client menu must stack category and cards on mobile')
assert.match(clientMenu, /\.dish-grid[\s\S]*?grid-template-columns:\s*1fr/, 'client menu cards must become one column on small screens')
assert.match(clientMenu, /\.cart-bar[\s\S]*?box-sizing:\s*border-box/, 'cart bar padding must stay inside the viewport')
assert.match(clientMenu, /\.cart-bar[\s\S]*?left:\s*12px[\s\S]*?right:\s*12px[\s\S]*?width:\s*auto/, 'mobile cart bar must use insets instead of an overflowing width')
assert.match(clientMenu, /\.cart-price[\s\S]*?min-width:\s*0/, 'mobile cart total must be allowed to shrink beside the checkout button')
assert.match(clientMenu, /@media\s*\(max-width:\s*480px\)/, 'client menu must tune very small phone widths')

assert.match(dishPage, /@media\s*\(max-width:\s*768px\)/, 'dish management page must define a mobile breakpoint')
assert.match(dishPage, /\.header-action[\s\S]*?flex-direction:\s*column/, 'dish management filters/actions must stack on mobile')
