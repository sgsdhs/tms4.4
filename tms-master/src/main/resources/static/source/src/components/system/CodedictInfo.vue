<template>
  <div>
    <div style="margin-bottom: 10px;text-align: left ">
      <el-button class="el-icon-back" type="primary" @click="back()">返回</el-button>
      <el-button plain class="el-icon-plus" @click="openDialog('add')">新建</el-button>
    </div>

    <el-table
      :data="tableData"
      style="width: 100%"
      @selection-change="handleSelectionChange">
      <el-table-column
        fixed="left"
        label="操作"
        width="150">
        <template slot-scope="scope">
          <el-button type="text" size="small" @click="openDialog('edit', scope.row)">编辑</el-button>
          <el-button type="text" size="small" @click="delData(scope.row)">删除</el-button>
        </template>
      </el-table-column>

      <el-table-column prop="category_id" label="类别代码" align="left"></el-table-column>
      <el-table-column prop="category_name" label="类别名称" align="left"></el-table-column>
      <el-table-column prop="code_key" label="代码key" align="left"></el-table-column>
      <el-table-column prop="code_value" label="代码value" align="left"></el-table-column>
    </el-table>
    <el-pagination style="margin-top: 10px; text-align: right;"
                   :current-page="currentPage"
                   @size-change="handleSizeChange"
                   @current-change="handleCurrentChange"
                   :page-sizes="[10, 25, 50, 100]"
                   :page-size="pageSize"
                   layout="total, sizes, prev, pager, next, jumper"
                   :total="total">
    </el-pagination>

    <el-dialog :title="dialogTitle" :visible.sync="dictDialogVisible" :close-on-click-modal="false">
      <el-form :model="dictDialogForm" :rules="rules" ref="dictDialogForm">
        <el-form-item label="代码类别key:" :label-width="formLabelWidth" prop="category_id">
          <el-input v-model="dictDialogForm.category_id" auto-complete="off" :disabled="true" :maxlength=50></el-input>
        </el-form-item>
        <el-form-item label="代码key:" :label-width="formLabelWidth" prop="code_key">
          <el-input v-model="dictDialogForm.code_key" auto-complete="off" :maxlength=50></el-input>
        </el-form-item>
        <el-form-item label="代码value:" :label-width="formLabelWidth" prop="code_value">
          <el-input v-model="dictDialogForm.code_value" auto-complete="off" :maxlength=50></el-input>
        </el-form-item>
        <el-form-item label="顺序:" :label-width="formLabelWidth" prop="onum">
          <el-input v-model="dictDialogForm.onum" auto-complete="off" :maxlength=3></el-input>
        </el-form-item>
        <el-form-item label="描述信息:" :label-width="formLabelWidth" prop="info">
          <el-input type="textarea" v-model="dictDialogForm.info"  :maxlength=200></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dictDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitForm('dictDialogForm')">保 存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
  import ajax from '@/common/ajax'
  import check from '@/common/check'

  export default {
    computed: {
      notSelectOne () {
        return this.selectedRows.length !== 1
      }
    },
    data () {
      return {
        tableData: [],
        dialogTitle: '',
        dictDialogVisible: false,
        formLabelWidth: '130px',
        dialogType: '',
        currentPage: 1,
        pageSize: 10,
        total: 0,
        selectedRows: [],
        categoryId: '',
        dictDialogForm: {
          code_id: '',
          category_id: '',
          code_key: '',
          code_value: '',
          onum: '',
          info: ''
        },
        rules: {
          category_id: [
            { required: true, message: '请输入代码类别key', trigger: 'blur' },
            { max: 50, message: '长度在50个字符以内', trigger: 'blur' },
            { validator: check.checkFormSpecialCode, trigger: 'blur' }
          ],
          code_key: [
            { required: true, message: '请输入代码key', trigger: 'blur' },
            { max: 50, message: '长度在50个字符以内', trigger: 'blur' },
            { validator: check.checkFormSpecialCode, trigger: 'blur' }
          ],
          code_value: [
            { required: true, message: '请输入代码value', trigger: 'blur' },
            { max: 50, message: '长度在50个字符以内', trigger: 'blur' },
            { validator: check.checkFormSpecialCode, trigger: 'blur' }
          ],
          onum: [
            { pattern: /^[1-9]\d{0,2}$/, message: '顺序最多只能输入3位数字' }
          ],
          info: [
            { max: 200, message: '长度在200个字符以内', trigger: 'blur' },
            { validator: check.checkFormSpecialCode, trigger: 'blur' }
          ]
        }
      }
    },
    mounted: function () {
      this.$nextTick(function () {
        this.categoryId = this.$route.query.category_id
        this.getData()
      })
    },
    methods: {
      handleSizeChange (val) {
        this.currentPage = 1
        this.pageSize = val
        this.getData()
      },
      handleCurrentChange (val) {
        this.currentPage = val
        this.getData()
      },
      handleSelectionChange (rows) {
        this.selectedRows = rows
      },
      bindGridData (data) {
        this.tableData = data.page.list
        this.currentPage = data.page.index
        this.pageSize = data.page.size
        this.total = data.page.total
      },
      initDialogForm () {
        return {
          category_id: '',
          code_key: '',
          code_value: '',
          onum: '',
          info: ''
        }
      },
      openDialog (dialogType, row) {
        this.dialogType = dialogType
        if (dialogType === 'edit') {
          this.dialogTitle = '编辑字典信息'
          // 拷贝而不是赋值
          Object.assign(this.dictDialogForm, row)
        } else if (dialogType === 'add') {
          this.dialogTitle = '新建字典信息'
          this.dictDialogForm = this.initDialogForm()
          this.dictDialogForm.category_id = this.categoryId
        }
        this.dictDialogVisible = true
        if (this.$refs['dictDialogForm']) {
          this.$refs['dictDialogForm'].clearValidate()
        }
      },
      submitForm (formName) {
        if (this.dialogType === 'add') {
          this.addData(formName)
        } else if (this.dialogType === 'edit') {
          this.updData(formName)
        }
      },
      addData (formName) {
        this.$refs[formName].validate((valid) => {
          if (valid) {
            let self = this
            let paramsObj = this.dictDialogForm
            ajax.post({
              url: '/codedict/code/add',
              param: paramsObj,
              success: function (data) {
                self.getData()
                self.$message.success('添加成功')
                self.dictDialogVisible = false
              }
            })
          } else {
            return false
          }
        })
      },
      getData () {
        let self = this
        let paramsObj = {
          categoryId: this.categoryId,
          pageindex: this.currentPage,
          pagesize: this.pageSize
        }
        ajax.post({
          url: '/codedict/category/codelist',
          param: paramsObj,
          success: function (data) {
            if (data.page) {
              self.bindGridData(data)
            }
          }
        })
      },
      delData (data) {
        let self = this
        this.$confirm('确定删除?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          ajax.post({
            url: '/codedict/code/del',
            param: {
              codeId: data.code_id
            },
            success: function (data) {
              self.$message.success('删除成功')
              self.getData()
            }
          })
        }).catch(() => {
          this.$message({
            type: 'info',
            message: '已取消删除'
          })
        })
      },
      updData (formName) {
        this.$refs[formName].validate((valid) => {
          if (valid) {
            let self = this
            let paramsObj = this.dictDialogForm
            ajax.post({
              url: '/codedict/code/update',
              param: paramsObj,
              success: function (data) {
                self.getData()
                self.$message.success('编辑成功')
                self.dictDialogVisible = false
              }
            })
          } else {
            return false
          }
        })
      },
      back () {
        this.$router.push({name: 'codedict'})
      }
    }
  }
</script>

<style>

</style>
