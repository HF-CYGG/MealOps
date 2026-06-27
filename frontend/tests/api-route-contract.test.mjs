import assert from 'node:assert/strict'
import { readFileSync, readdirSync } from 'node:fs'
import { dirname, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const frontendRoot = resolve(__dirname, '..')
const projectRoot = resolve(frontendRoot, '..')

const normalizePath = (path) => {
  let normalized = path
    .replace(/\$\{[^}]+}/g, '{}')
    .replace(/\{[^}/]+}/g, '{}')
    .replace(/\/+/g, '/')
  if (normalized.length > 1 && normalized.endsWith('/')) {
    normalized = normalized.slice(0, -1)
  }
  return normalized
}

const parseJavaStringArray = (text) => {
  const matches = [...text.matchAll(/"([^"]+)"/g)].map(match => match[1])
  return matches.length > 0 ? matches : ['']
}

const controllerRoutes = () => {
  const controllerDir = resolve(projectRoot, 'src/main/java/com/cjc/mealops/controller')
  const routes = new Set()

  for (const fileName of readdirSync(controllerDir).filter(name => name.endsWith('.java'))) {
    const source = readFileSync(resolve(controllerDir, fileName), 'utf8')
    const classMapping = source.match(/@RequestMapping\("([^"]+)"\)\s*public class/)
    const classBase = classMapping ? classMapping[1] : ''

    for (const match of source.matchAll(/@(GetMapping|PostMapping|PutMapping|DeleteMapping|PatchMapping)\s*(?:\(([\s\S]*?)\))?\s+public/g)) {
      const paths = match[2] ? parseJavaStringArray(match[2]) : ['']
      for (const path of paths) {
        routes.add(normalizePath(`${classBase}/${path}`))
      }
    }

    for (const match of source.matchAll(/@RequestMapping\(\s*value\s*=\s*"([^"]+)"/g)) {
      routes.add(normalizePath(`${classBase}/${match[1]}`))
    }
  }

  return routes
}

const frontendApiRoutes = () => {
  const apiDir = resolve(frontendRoot, 'src/api')
  const routes = new Set()

  for (const fileName of readdirSync(apiDir).filter(name => name.endsWith('.js'))) {
    const source = readFileSync(resolve(apiDir, fileName), 'utf8')
    for (const match of source.matchAll(/url:\s*(['`])([^'`]+)\1/g)) {
      routes.add(normalizePath(match[2]))
    }
  }

  return routes
}

const staticPageRoutes = () => {
  const source = readFileSync(resolve(projectRoot, 'src/main/resources/static/assets/js/api.js'), 'utf8')
  const routes = new Set()

  const expressionToRoute = (expression) => {
    return normalizePath(expression
      .split('+')
      .map(part => {
        const trimmed = part.trim()
        const stringMatch = trimmed.match(/^"([^"]*)"$/)
        return stringMatch ? stringMatch[1] : '{}'
      })
      .join(''))
  }

  for (const match of source.matchAll(/\b(?:get|post|put|del)\(([^,\n]+(?:\+[^,\n]+)*)/g)) {
    if (match[1].trim().startsWith('"')) {
      const route = expressionToRoute(match[1])
      if (route.startsWith('/')) {
        routes.add(route)
      }
    }
  }
  for (const match of source.matchAll(/\brequest\("[A-Z]+",\s*([^,\n]+(?:\+[^,\n]+)*)/g)) {
    if (match[1].trim().startsWith('"')) {
      const route = expressionToRoute(match[1])
      if (route.startsWith('/')) {
        routes.add(route)
      }
    }
  }

  return routes
}

const backendRoutes = controllerRoutes()
const apiRoutes = new Set([
  ...frontendApiRoutes(),
  ...staticPageRoutes()
])

const intentionallyLocal = new Set(['/health'])
const missingRoutes = [...apiRoutes]
  .filter(route => !intentionallyLocal.has(route))
  .filter(route => !backendRoutes.has(route))
  .sort()

assert.deepEqual(
  missingRoutes,
  [],
  `frontend/static API routes must exist in backend controllers: ${missingRoutes.join(', ')}`
)
