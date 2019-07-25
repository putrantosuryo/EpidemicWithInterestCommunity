/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package routing.interest;

import core.DTNHost;
import java.util.Map;

/**
 *
 * @author E5
 */
public interface ProportionDetectionEgine {
    public Map<DTNHost, Double>  getProportion();
}
