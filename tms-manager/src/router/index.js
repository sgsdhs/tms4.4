import Vue from 'vue'
import Router from 'vue-router'

// 引入组件
import Login from '@/frame/Login'
import Main from '@/frame/Main'

import User from '@/components/auth/User'
import Role from '@/components/auth/Role'
import Func from '@/components/auth/Func'

import List from '@/components/run/List'
import ValueList from '@/components/run/ValueList'
import Txn from '@/components/run/Txn'
import Rule from '@/components/run/Rule'
import Stat from '@/components/run/Stat'
import Trandef from '@/components/run/Trandef'
import Tranmdl from '@/components/run/Tranmdl'


import AuthCenter from '@/components/system/AuthCenter'
import AuthDataList from '@/components/system/AuthDataList'
import AuthLogList from '@/components/system/AuthLogList'
import AuthSubDataList from '@/components/system/AuthSubDataList'
import AuthDataCompare from '@/components/system/AuthDataCompare'
import Codedict from '@/components/system/Codedict'
import CodedictInfo from '@/components/system/CodedictInfo'
import Ipaddr from '@/components/system/Ipaddr'
import Log from '@/components/system/Log'

import TxnEventQuery from '@/components/synquery/TxnEventQuery'

import NotFoundComponent from '@/components/NotFoundComponent'
import util from '@/common/util'

Vue.use(Router)

export default new Router({
  mode: 'history',
  base: util.getWebRootPath(),
  linkActiveClass: 'active',
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: Login
    },
    {
      path: '/',
      name: 'Main',
      component: Main,
      meta: {
        requireAuth: true  // 添加该字段，表示进入这个路由是需要登录的
      },
      children: [
        {
          path: 'user',
          name: 'User',
          component: User
        },
        {
          path: 'role',
          name: 'Role',
          component: Role
        },
        {
          path: 'func',
          name: 'Func',
          component: Func
        },
        {
          path: 'valuelist/:rosterid/:datatype',
          name: 'ValueList',
          component: ValueList
        },
        {
          path: 'list',
          name: 'List',
          component: List
        },
        {
          path: 'txn',
          name: 'Txn',
          component: Txn
        },
        {
          path: 'authCenter.vue',
          name: 'AuthCenter',
          component: AuthCenter
        },
        {
          path: 'authDataCompare.vue',
          name: 'AuthDataCompare',
          component: AuthDataCompare
        },
        {
          path: 'authDataList.vue',
          name: 'AuthDataList',
          component: AuthDataList
        },
        {
          path: 'authLogList.vue',
          name: 'AuthLogList',
          component: AuthLogList
        },
        {
          path: 'authSubDataList.vue',
          name: 'AuthSubDataList',
          component: AuthSubDataList
        },
        {
          path: 'codedict.vue',
          name: 'Codedict',
          component: Codedict
        },
        {
          path: 'codeDictInfo.vue',
          name: 'CodeDictInfo',
          component: CodedictInfo
        },
        {
          path: 'ipaddr.vue',
          name: 'Ipaddr',
          component: Ipaddr
        },
        {
          path: 'log',
          name: 'Log',
          component: Log
        },
        {
          path: 'txnEventQuery',
          name: 'TxnEventQuery',
          component: TxnEventQuery
        }
      ]
    },
    {
      path: '*',
      name: 'NotFound',
      component: NotFoundComponent
    }
  ]
})
