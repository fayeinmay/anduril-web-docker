<template>
  <div class="item">
    <Icon :icon="iconType" />
    <div class="ribbon">
      <slot name="subType"></slot>
    </div>
    <div class="details">
      <h3>
        <slot name="name"></slot>
      </h3>
      <p>
        <slot name="ip"></slot>
      </p>
      <p>
        <slot name="mac"></slot>
      </p>
    </div>
  </div>
</template>

<script>
import { Icon } from '@iconify/vue'

export default {
  components: { Icon },
  props: {
    iconType: String
  }
}
</script>

<style scoped>
.item {
  margin-top: 1rem;
  display: flex;
  position: relative;
}

.details {
  flex: 1;
  margin-left: 1rem;
}

svg {
  display: flex;
  place-items: center;
  place-content: center;
  width: 32px;
  height: 32px;

  color: var(--color-text);
}

h3 {
  font-size: 0.8rem;
  font-weight: 500;
  margin-bottom: 0.4rem;
  color: var(--color-heading);
}

.ribbon {
  position: absolute;
  display: flex;
  place-items: center;
  place-content: start;
  width: 50px;
  top: 20px;
  left: 25px;

  font-size: 10px;
  font-weight: bold;
  color: var(--color-heading);
  text-align: center;
}

@media (min-width: 1024px) {
  h3 {
    font-size: 1.3rem;
    font-weight: 500;
    margin-bottom: 0.4rem;
    color: var(--color-heading);
  }

  .item {
    margin-top: 0;
    padding: 0.4rem 0 1rem calc(var(--section-gap) / 3);
  }

  svg {
    top: calc(50% - 25px);
    left: 0;
    position: absolute;
    background: var(--color-background);
    width: 50px;
    height: 50px;
  }

  .ribbon {
    position: absolute;
    display: flex;
    place-items: center;
    place-content: start;
    width: 50px;
    top: calc(60%);
    left: 35px;

    font-size: 10px;
    font-weight: bold;
    color: var(--color-heading);
    text-align: center;
  }
}
</style>
