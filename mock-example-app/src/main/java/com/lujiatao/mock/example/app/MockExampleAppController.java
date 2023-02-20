package com.lujiatao.mock.example.app;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Mock示例应用控制器
 *
 * @author 卢家涛
 */
@RestController
@RequestMapping("/mock-example-app")
@ResponseBody
public class MockExampleAppController {

    private static final List<MockExampleAppVO> mockExampleAppVOS = Arrays.asList(
            new MockExampleAppVO(1, "String Parameter 0", 0, 10, 0.0, 1.0, true, false),
            new MockExampleAppVO(2, "String Parameter 1", 1, 11, 0.1, 1.1, false, true),
            new MockExampleAppVO(3, "String Parameter 2", 2, 12, 0.2, 1.2, true, false),
            new MockExampleAppVO(4, "String Parameter 3", 3, 13, 0.3, 1.3, false, true),
            new MockExampleAppVO(5, "String Parameter 4", 4, 14, 0.4, 1.4, true, false),
            new MockExampleAppVO(6, "String Parameter 5", 5, 15, 0.5, 1.5, false, true),
            new MockExampleAppVO(7, "String Parameter 6", 6, 16, 0.6, 1.6, true, false),
            new MockExampleAppVO(8, "String Parameter 7", 7, 17, 0.7, 1.7, false, true),
            new MockExampleAppVO(9, "String Parameter 8", 8, 18, 0.8, 1.8, true, false),
            new MockExampleAppVO(10, "String Parameter 9", 9, 19, 0.9, 1.9, false, true)
    );

    @GetMapping("/all")
    public List<MockExampleAppVO> getAll() {
        return mockExampleAppVOS;
    }

    @GetMapping("/by-conditions")
    public List<MockExampleAppVO> getByConditions(@RequestParam("stringParameter") String stringParameter,
                                                  @RequestParam("intParameter") int intParameter,
                                                  @RequestParam("integerParameter") Integer integerParameter,
                                                  @RequestParam("doubleParameter") double doubleParameter,
                                                  @RequestParam("aDoubleParameter") Double aDoubleParameter,
                                                  @RequestParam("booleanParameter") boolean booleanParameter,
                                                  @RequestParam("aBooleanParameter") Boolean aBooleanParameter) {
        List<MockExampleAppVO> result = new ArrayList<>();
        for (MockExampleAppVO mockExampleAppVO : mockExampleAppVOS) {
            if (mockExampleAppVO.getStringParameter().equals(stringParameter) &&
                    mockExampleAppVO.getIntParameter() == intParameter &&
                    mockExampleAppVO.getIntegerParameter().equals(integerParameter) &&
                    mockExampleAppVO.getDoubleParameter() == doubleParameter &&
                    mockExampleAppVO.getADoubleParameter().equals(aDoubleParameter) &&
                    mockExampleAppVO.isBooleanParameter() == booleanParameter &&
                    mockExampleAppVO.getABooleanParameter().equals(aBooleanParameter)) {
                result.add(mockExampleAppVO);
            }
        }
        return result;
    }

    @GetMapping("/by-id")
    public MockExampleAppVO getById(@RequestParam("id") int id) {
        for (MockExampleAppVO mockExampleAppVO : mockExampleAppVOS) {
            if (mockExampleAppVO.getId() == id) {
                return mockExampleAppVO;
            }
        }
        return null;
    }

    @GetMapping("/string")
    public String getString() {
        return "我是String";
    }

    @GetMapping("/byte")
    public byte getByte() {
        return 1;
    }

    @GetMapping("/a-byte")
    public Byte getAByte() {
        return 2;
    }

    @GetMapping("/short")
    public short getShort() {
        return 3;
    }

    @GetMapping("/a-short")
    public Short getAShort() {
        return 4;
    }

    @GetMapping("/int")
    public int getInt() {
        return 5;
    }

    @GetMapping("/integer")
    public Integer getInteger() {
        return 6;
    }

    @GetMapping("/long")
    public long getLong() {
        return 7L;
    }

    @GetMapping("/a-long")
    public Long getALong() {
        return 8L;
    }

    @GetMapping("/float")
    public float getFloat() {
        return 9.0F;
    }

    @GetMapping("/a-float")
    public Float getAFloat() {
        return 10.0F;
    }

    @GetMapping("/double")
    public double getDouble() {
        return 11.0;
    }

    @GetMapping("/a-double")
    public Double getADouble() {
        return 12.0;
    }

    @GetMapping("/boolean")
    public boolean getBoolean() {
        return true;
    }

    @GetMapping("/a-boolean")
    public Boolean getABoolean() {
        return false;
    }

    @GetMapping("/char")
    public char getChar() {
        return 'a';
    }

    @GetMapping("/character")
    public Character getCharacter() {
        return 'b';
    }

}
