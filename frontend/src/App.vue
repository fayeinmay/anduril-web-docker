<script setup></script>

<template>
  <div>
    Upload (only cfg and hwdef files!):
    <input type="file" @change="selectFilesForUpload" ref="file" multiple />
    <button @click="uploadFiles">Upload!</button>
    <ul>
      <li v-for="file in files" :key="file.name">{{ file.name }}</li>
    </ul>
    <button @click="resetFiles">Reset to git base state!</button>
    Uploaded files:
    <ul>
      <li v-for="file in onlineFiles" :key="file">
        {{ file.name }}
        <button @click="removeFile(file.name)">Delete</button>
        <button v-show="isCfg(file.name)" @click="buildFile(file.name)">Build</button>
        <button v-show="isCfg(file.name) && file.buildName" @click="downloadFile(file.buildName)">Download hex</button>
        {{ file.logs }}
      </li>
    </ul>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  data() {
    return {
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
    selectFilesForUpload(e) {
      if (this.isValidFiles(e.target.files)) {
        this.files = e.target.files
      } else {
        this.resetUploadState()
      }
    },
    isValidFile(name) {
      return name.startsWith('cfg') || name.startsWith('hwdef')
    },
    isValidFiles(files) {
      for (let file of files) {
        if (!this.isValidFile(file.name)) {
          return false
        }
      }
      return true
    },
    uploadFiles() {
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
        .delete(`/api/v1/${name}`)
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
      this.$refs.file.value = null
    }
  }
}
</script>

<style></style>
