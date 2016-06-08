/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ijfx.ui.filter.string;

import ijfx.ui.filter.StringFilter;
import ijfx.ui.utils.BaseTester;
import static java.lang.Math.random;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 * @author tuananh
 */
public class StringFilterTester extends BaseTester {

    StringFilter stringFilter;

    public static void main(String[] args) {
        launch(args);

    }

    public StringFilterTester() {
        super();
         addAction("Update values",this::updateValues);
    }
    
    @Override
    public void initApp() {
        stringFilter = new DefaultStringFilter();
        setContent(stringFilter.getContent());
        updateValues();
    }
    
    private void updateValues() {
          final SecureRandom random = new SecureRandom();

        List<String> collect = IntStream.range(0, 30)
                .mapToObj(i -> new BigInteger(130, random).toString(32))
                .collect(Collectors.toList());
        stringFilter.setAllPossibleValues(collect);
    }

}
