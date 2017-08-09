package tarce.model.inventory;

import java.util.List;

/**
 * Created by zws on 2017/8/9.
 */

public class NewSaleBean {
    /**
     * jsonrpc : 2.0
     * id : null
     * result : {"res_data":[{"team_id":7,"name":"其他销售"},{"team_id":3,"name":"内销团队"},{"team_id":5,"name":"外销团队"},{"team_id":4,"name":"电商阿里"},{"team_id":2,"name":"电商零售"},{"team_id":6,"name":"跨境电商"}],"res_msg":"","res_code":1}
     */

    private String jsonrpc;
    private Object id;
    private ResultBean result;

    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public ResultBean getResult() {
        return result;
    }

    public void setResult(ResultBean result) {
        this.result = result;
    }

    public static class ResultBean {
        /**
         * res_data : [{"team_id":7,"name":"其他销售"},{"team_id":3,"name":"内销团队"},{"team_id":5,"name":"外销团队"},{"team_id":4,"name":"电商阿里"},{"team_id":2,"name":"电商零售"},{"team_id":6,"name":"跨境电商"}]
         * res_msg :
         * res_code : 1
         */

        private String res_msg;
        private int res_code;
        private List<ResDataBean> res_data;

        public String getRes_msg() {
            return res_msg;
        }

        public void setRes_msg(String res_msg) {
            this.res_msg = res_msg;
        }

        public int getRes_code() {
            return res_code;
        }

        public void setRes_code(int res_code) {
            this.res_code = res_code;
        }

        public List<ResDataBean> getRes_data() {
            return res_data;
        }

        public void setRes_data(List<ResDataBean> res_data) {
            this.res_data = res_data;
        }

        public static class ResDataBean {
            /**
             * team_id : 7
             * name : 其他销售
             */

            private int team_id;
            private String name;

            public int getTeam_id() {
                return team_id;
            }

            public void setTeam_id(int team_id) {
                this.team_id = team_id;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }
        }
    }
}
