import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import pinia from './stores'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './style.css'
import './permission'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'

const app = createApp(App)

pinia.use(piniaPluginPersistedstate)
app.use(router)
app.use(pinia)
app.use(ElementPlus)

app.mount('#app')
