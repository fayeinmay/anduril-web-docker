<script setup></script>

<template>
  <v-app>
    <v-app-bar app color="white" flat>
      <v-container>
        <v-form ref="form">
          <v-row align="center">
            <v-col cols="8">
              <v-file-input
                :rules="fileRules"
                v-model="files"
                :chips="true"
                :multiple="true"
                label="Select only cfg or hwdef files!"
                class="mt-6"
              />
            </v-col>
            <v-col cols="1">
              <v-btn :disabled="$refs.form ? !$refs.form.isValid : true" @click="uploadFiles"
                >Upload</v-btn
              >
            </v-col>
            <v-col cols="3">
              <v-btn @click="resetFiles">Reset everything</v-btn>
            </v-col>
          </v-row>
        </v-form>
      </v-container>
    </v-app-bar>
    <v-main class="bg-grey-lighten-4">
      <v-container>
        <v-row>
          <v-col cols="4">
            <v-sheet rounded="lg">
              <v-list rounded="lg">
                <v-list-item
                  v-for="file in onlineFiles"
                  :key="file"
                  :title="file.name"
                  :link="true"
                  @click="selectLog(file.name)"
                >
                  <v-btn @click="removeFile(file.name)" class="ma-1">Delete</v-btn>
                  <v-btn v-show="isCfg(file.name)" @click="buildFile(file.name)" class="ma-1"
                    >Build</v-btn
                  >
                  <v-btn
                    v-show="isCfg(file.name) && file.buildName"
                    @click="downloadFile(file.buildName)"
                    class="ma-1"
                  >
                    Download hex
                  </v-btn>
                </v-list-item>
              </v-list>
            </v-sheet>
          </v-col>

          <v-col>
            <v-sheet min-height="70vh" rounded="lg" class="text-center">
              <v-textarea
                label="Logs | Click on an entry on the left side after building"
                :readonly="true"
                variant="outlined"
                auto-grow
                rows="30"
                v-model="currentLog"
              ></v-textarea>
            </v-sheet>
          </v-col>
        </v-row>
      </v-container>
    </v-main>
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
      currentLog: [],
      files: [],
      onlineFiles: []
    }
  },
  mounted() {
    this.getFiles()
  },
  methods: {
    selectLog(name) {
      this.currentLog = this.onlineFiles.find((c) => c.name === name).logs
    },
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
          this.selectLog(name)
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
