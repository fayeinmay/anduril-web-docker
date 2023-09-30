<script setup>
import DeviceItem from './components/DeviceItem.vue'
</script>

<template>
  <h2>Last Updated: {{ new Date(lastUpdated).toLocaleString() }}</h2>
  <DeviceItem v-for="device in devices" :key="device.ip" :iconType="device.icon">
    <template #subType>{{ device.subType }}</template>
    <template #name>{{ device.name }}</template>
    <template #ip>{{ device.ip }}</template>
    <template #mac>{{ device.mac }}</template>
  </DeviceItem>
</template>

<script>
import axios from 'axios'
import questionMark from '@iconify-icons/tabler/question-mark'
import ethernetIcon from '@iconify-icons/fa-solid/ethernet'
import wifiOff from '@iconify-icons/tabler/wifi-off'
import wifi0 from '@iconify-icons/tabler/wifi-0'
import wifi1 from '@iconify-icons/tabler/wifi-1'
import wifi2 from '@iconify-icons/tabler/wifi-2'
import wifi from '@iconify-icons/tabler/wifi'

export default {
  data() {
    return {
      devices: [],
      lastUpdated: null,
      darkMode: '#181818',
      darkModeInverted: '#ffffff',
      icons: {
        questionMark,
        ethernetIcon,
        wifiOff,
        wifi0,
        wifi1,
        wifi2,
        wifi
      }
    }
  },
  mounted() {
    this.getDevices()

    let queryString = window.location.search
    let urlParams = new URLSearchParams(queryString)

    if (urlParams.get('dark') === 'true') {
      this.darkMode = '#ffffff'
      this.darkModeInverted = '#181818'
      // Don't see another way?
      document.body.style.backgroundColor = '#181818'
      document.body.style.color = '#ffffff'
    }
  },
  methods: {
    getDevices() {
      axios
        .get('/api/v1/devices')
        .then((response) => {
          if (response.data) {
            for (let device of response.data.devices) {
              if (device.type === 'ETHERNET') {
                device.icon = this.icons.ethernetIcon
              } else if (device.type === 'WIFI') {
                this.setSubType(device)
                this.setSignalStrength(device)
              } else {
                device.icon = this.icons.questionMark
              }
            }

            this.devices = response.data.devices
            this.lastUpdated = response.data.lastUpdated
          }
        })
        .catch((e) => {
          console.log(e)
        })
    },
    setSubType(device) {
      if (device.subType === 'WIFI_2_4') {
        device.subType = '2.4'
      } else if (device.subType === 'WIFI_5') {
        device.subType = '5'
      }
    },
    setSignalStrength(device) {
      if (device.signalStrength) {
        switch (device.signalStrength) {
          case 1:
            device.icon = this.icons.wifi0
            break
          case 2:
            device.icon = this.icons.wifi1
            break
          case 3:
            device.icon = this.icons.wifi1
            break
          case 4:
            device.icon = this.icons.wifi2
            break
          case 5:
            device.icon = this.icons.wifi
            break
        }
      } else {
        device.icon = this.icons.wifiOff
      }
    }
  }
}
</script>

<style>
.ribbon {
  color: v-bind('darkMode') !important;
}
h2 {
  color: v-bind('darkMode') !important;
  display: flex;
  place-items: center;
  place-content: center;
  grid-column: 1/-1;
}
h3 {
  color: v-bind('darkMode') !important;
}
svg {
  background: v-bind('darkModeInverted') !important;
  color: v-bind('darkMode') !important;
}
</style>
