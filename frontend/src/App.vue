<script setup></script>

<template>
  <v-app>
    <v-layout class="rounded rounded-md">
      <v-main>
        <v-form ref="form">
          <v-file-input
            :rules="fileRules"
            v-model="files"
            :chips="true"
            :multiple="true"
            label="Select only cfg or hwdef files!"
          />
          <v-btn :disabled="$refs.form ? !$refs.form.isValid : true" @click="uploadFiles"
            >UPLOAD</v-btn
          >
        </v-form>

        <v-btn @click="resetFiles">Reset to git base state!</v-btn>
        <v-list>
          <v-list-item v-for="file in onlineFiles" :key="file" :title="file.name">
            <v-btn @click="removeFile(file.name)">Delete</v-btn>
            <v-btn v-show="isCfg(file.name)" @click="buildFile(file.name)">Build</v-btn>
            <v-btn
              v-show="isCfg(file.name) && file.buildName"
              @click="downloadFile(file.buildName)"
            >
              Download hex
            </v-btn>
            <p v-for="(log, index) in file.logs" :key="index">
              {{ log }}
            </p>
          </v-list-item>
        </v-list>
      </v-main>
    </v-layout>
  </v-app>
</template>

<script>
import axios from 'axios'

export default {
  data() {
    return {
      fileRules: [
        (value) => {
          return value.every((i) => i.name.startsWith('cfg') || i.name.startsWith('hwdef'))
        }
      ],
      files: [],
      onlineFiles: []
    }
  },
  mounted() {
    this.getFiles()
  },
  methods: {
    isCfg(name) {
      return name.includes('cfg')
    },
    uploadFiles() {
      if (this.files.isEmpty()) return

      const formData = new FormData()
      for (let file of this.files) {
        formData.append('file', file)
      }

      const headers = { 'Content-Type': 'multipart/form-data' }
      axios
        .post('/api/v1/files', formData, { headers })
        .then((res) => {
          this.getFiles()

          if (res.status === 200) {
            this.resetUploadState()
          }
        })
        .catch((e) => {
          console.log(e)
        })
    },
    getFiles() {
      axios
        .get('/api/v1/files')
        .then((res) => {
          this.onlineFiles = res.data
        })
        .catch((e) => {
          console.log(e)
        })
    },
    buildFile(name) {
      axios
        .patch(`/api/v1/files/${name}/build`)
        .then(() => {
          // Maybe wait because of race condition
          this.getFiles()
        })
        .catch((e) => {
          console.log(e)
        })
    },
    downloadFile(buildName) {
      axios
        .get(`/api/v1/files/${buildName}`)
        .then((res) => {
          const url = window.URL.createObjectURL(new Blob([res.data]))
          const link = document.createElement('a')
          link.href = url
          link.setAttribute('download', buildName)
          document.body.appendChild(link)
          link.click()
        })
        .catch((e) => {
          console.log(e)
        })
    },
    removeFile(name) {
      axios
        .delete(`/api/v1/files/${name}`)
        .then(() => {
          this.getFiles()
        })
        .catch((e) => {
          console.log(e)
        })
    },
    resetFiles() {
      axios
        .patch('/api/v1/files/reset')
        .then(() => {
          this.getFiles()
        })
        .catch((e) => {
          console.log(e)
        })
    },
    resetUploadState() {
      this.files = []
      this.$refs.file.reset()
    }
  }
}
</script>

<style></style>
