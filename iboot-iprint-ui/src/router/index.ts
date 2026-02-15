import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('../views/Login.vue')
    },
    {
      path: '/',
      component: () => import('../components/Layout.vue'),
      children: [
        { path: '', redirect: '/api-keys' },
        {
          path: 'api-keys',
          name: 'ApiKeys',
          component: () => import('../views/ApiKeys.vue')
        },
        {
          path: 'templates',
          name: 'PrintTemplates',
          component: () => import('../views/PrintTemplates.vue')
        },
        {
          path: 'change-password',
          name: 'ChangePassword',
          component: () => import('../views/ChangePassword.vue')
        }
      ]
    }
  ]
})

router.beforeEach(async (to) => {
  const authStore = useAuthStore()
  if (to.name !== 'Login' && !authStore.isLoggedIn) {
    await authStore.fetchUser()
    if (!authStore.isLoggedIn) {
      return { name: 'Login' }
    }
  }
})

export default router
