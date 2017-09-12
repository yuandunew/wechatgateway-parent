package com.yuandu.user_api.remote;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "user")
public interface UserServiceRemote {

    @RequestMapping(value = "/group/{groupId}", method = RequestMethod.GET)
    String findByGroupId(@PathVariable("groupId") Integer adGroupId);

}
