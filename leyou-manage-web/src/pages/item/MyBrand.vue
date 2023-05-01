<template>
  <v-card>
    <v-card-title>
      <v-btn color="info">新增品牌</v-btn>
      <v-spacer></v-spacer>
      <v-text-field
        v-model="search"
        append-icon="search"
        label="Search"
        single-line
        hide-details
      ></v-text-field>
    </v-card-title>
    <v-data-table
      :headers="headers"
      :items="brands"
      :pagination.sync="pagination"
      :total-items="totalBrands"
      :loading="loading"
      class="elevation-1"
    >
      <template slot="items" slot-scope="props">
        <td class="text-xs-center">{{ props.item.id }}</td>
        <td class="text-xs-center">{{ props.item.name }}</td>
        <td class="text-xs-center"><img :src="props.item.image" /></td>
        <td class="text-xs-center">{{ props.item.letter }}</td>
        <td class="text-xs-center"></td>
      </template>
    </v-data-table>
  </v-card>
</template>
<script>
  export default {
    data () {
      return {
        totalBrands: 0,
        brands: [],
        loading: true,
        pagination: {},
        headers: [
          {
            text: 'id',
            align: 'center',
            value: 'id'
          },
          { text: '品牌名称', sortable: false, align: 'center', value: 'name' },
          { text: 'LOGO', sortable: false, align: 'center', value: 'image' },
          { text: '首字母', align: 'center', value: 'letter' },
          { text: '操作', align: 'center', value: 'id' }
        ],
        search: ""
      }
    },
    watch: {
      pagination: {
        handler () {
          this.getDataFromApi();
        },
        deep: true
      },
      search: {
        handler () {
          this.getDataFromApi();
        }
      }
    },
    created () {
      this.getDataFromApi();
    },
    methods: {
      getDataFromApi () {
        this.$http.get("/item/brand/page", {
          params: {
            key: this.search,
            page: this.pagination.page,// 当前页
            rows: this.pagination.rowsPerPage,// 每页大小
            sortBy: this.pagination.sortBy,// 排序字段
            desc: this.pagination.descending// 是否降序
          }
        }).then(({data}) => {
          this.brands = data.items;
          this.totalBrands = data.total;
          this.loading = false;
        }).catch()
      },
    }
  }
</script>
