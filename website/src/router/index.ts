import { createRouter, createWebHistory } from 'vue-router'
import ChatView from '../views/ChatView.vue'
import KnowledgeBase from '../views/KnowledgeBase.vue'
import SettingsView from '../views/SettingsView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/chat',
    },
    {
      path: '/chat',
      name: 'chat',
      component: ChatView,
    },
    {
      path: '/knowledge',
      name: 'knowledge',
      component: KnowledgeBase,
    },
    {
      path: '/settings',
      name: 'settings',
      component: SettingsView,
    },
  ],
})

export default router
