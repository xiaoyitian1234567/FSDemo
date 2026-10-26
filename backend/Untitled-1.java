// 使用Spring的RestTemplate示例
@Autowired
private RestTemplate restTemplate;

public String getFeishuUser(String userId) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + getAccessToken());
    // 实现获取访问令牌和API调用逻辑
    return restTemplate.getForObject("https://open.feishu.cn/open-apis/contact/v3/users/" + userId, String.class);
}