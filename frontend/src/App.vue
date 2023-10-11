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
    <v-main class="bg-grey-lighten-3">
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
                  @click="openFile(file.name)"
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
            <v-sheet min-height="50vh" rounded="lg">
              <VLayoutItem
                v-if="activeFile && activeFile.flags"
                :model-value="activeFile.flags.length !== 0"
                position="bottom"
                class="text-end"
                size="88"
                style="z-index: 0"
              >
                <v-btn
                  @click="saveFlags()"
                  icon="mdi-content-save"
                  size="large"
                  color="primary"
                  elevation="8"
                  class="ma-3"
                />
              </VLayoutItem>
              <v-list v-if="activeFile && activeFile.flags" rounded="lg" style="column-count: 2">
                <v-list-item v-for="flag in activeFile.flags" :key="flag">
                  <v-list-item-title class="text-wrap">
                    {{ flag.fileName }}, Line {{ flag.line + 1 }}, {{ flag.name }}
                  </v-list-item-title>
                  <template v-slot:prepend>
                    <v-list-item-action>
                      <v-checkbox-btn v-model:model-value="flag.defined"></v-checkbox-btn>
                    </v-list-item-action>
                  </template>
                  <v-text-field
                    :disabled="!flag.defined"
                    v-model:model-value="flag.value"
                  ></v-text-field>
                </v-list-item>
              </v-list>
            </v-sheet>
            <v-sheet min-height="30vh" rounded="lg" class="text-center mt-6">
              <v-textarea
                v-if="activeFile"
                label="Logs | Click on an entry on the left side after building"
                :readonly="true"
                variant="outlined"
                auto-grow
                rows="1"
                :model-value="activeFile.logs ? activeFile.logs : ''"
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
      currentFlags: [],
      activeFile: null,
      files: [],
      onlineFiles: []
    }
  },
  mounted() {
    this.getFiles()
  },
  methods: {
    openFile(name) {
      this.activeFile = this.onlineFiles.find((c) => c.name === name)
      this.getFlags(this.activeFile)
    },
    isCfg(name) {
      return name.startsWith('cfg')
    },
    uploadFiles() {
      if (this.files.length === 0) return

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
    getFlags(activeFile) {
      axios
        .get(`/api/v1/files/${activeFile.name}/flags`)
        .then((res) => {
          activeFile.flags = res.data
        })
        .catch((e) => {
          console.log(e)
        })
    },
    saveFlags() {
      if (!this.activeFile?.flags) return

      axios
        .post(`/api/v1/files/${this.activeFile.name}/flags`, this.activeFile.flags)
        .then(() => {
          this.getFiles()
          this.openFile(this.activeFile.name) // Reopen afterwards, TODO: Handle proper closing on remove etc., maybe add some loading screen
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
          this.openFile(name)
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
